package stakemate.use_case.fetch_games;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import stakemate.data_access.api.OddsApiResponseAdapter;
import stakemate.entity.Game;

/**
 * Interactor for the FetchGames use case.
 * Orchestrates the flow: API -> Adapter -> Repository -> Presenter
 *
 * <p>
 * TODO: Add comprehensive unit tests for this interactor (Use Case 1):
 *   - Test fetchAndUpdateGames() with valid sport/region/date parameters
 *   - Test fetchAndUpdateGames() with null/empty sport (should fail)
 *   - Test fetchAndUpdateGames() when API returns empty list
 *   - Test fetchAndUpdateGames() when API throws ApiException
 *   - Test fetchAndUpdateGames() when repository throws RepositoryException
 *   - Test refreshGames() delegates to fetchAndUpdateGames with defaults
 *   - Test searchGames() with valid query
 *   - Test searchGames() when repository throws exception
 *   - Test normalizeAndValidateGames() filters out invalid games
 *   - Verify presenter methods are called with correct data
 * TODO: Fix any Checkstyle violations in this file
 */
public class FetchGamesInteractor implements FetchGamesInputBoundary {

    // Default values for refresh
    private static final String DEFAULT_SPORT = "basketball_nba";
    private static final String DEFAULT_REGION = "us";
    private final OddsApiGateway apiGateway;
    private final OddsApiResponseAdapter responseAdapter;
    private final GameRepository gameRepository;
    private final FetchGamesOutputBoundary presenter;

    public FetchGamesInteractor(final OddsApiGateway apiGateway,
                                final OddsApiResponseAdapter responseAdapter,
                                final GameRepository gameRepository,
                                final FetchGamesOutputBoundary presenter) {
        this.apiGateway = apiGateway;
        this.responseAdapter = responseAdapter;
        this.gameRepository = gameRepository;
        this.presenter = presenter;
    }

    @Override
    public void fetchAndUpdateGames(final String sport, final String region, final LocalDate dateFrom) {
        presenter.presentFetchInProgress();

        try {
            processFetchAndUpdate(sport, region, dateFrom);
        }
        catch (final ApiException ex) {
            presenter.presentFetchError("API error: " + ex.getMessage());
        }
        catch (final RepositoryException ex) {
            presenter.presentFetchError("Database error: " + ex.getMessage());
        }
        catch (final Exception ex) {
            presenter.presentFetchError("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Processes the fetch and update logic.
     *
     * @param sport The sport to fetch
     * @param region The region to filter by
     * @param dateFrom The date to fetch from
     * @throws ApiException if API call fails
     * @throws RepositoryException if database operation fails
     */
    private void processFetchAndUpdate(final String sport, final String region, final LocalDate dateFrom)
            throws ApiException, RepositoryException {
        // Check if API gateway is configured
        if (apiGateway == null) {
            presenter.presentFetchError("API gateway not configured. Please set ODDS_API_KEY.");
            return;
        }

        // Validate sport parameter
        if (sport == null || sport.trim().isEmpty()) {
            presenter.presentFetchError("Sport parameter is required");
            return;
        }

        // Use today if dateFrom is null
        final LocalDate effectiveDate;
        if (dateFrom != null) {
            effectiveDate = dateFrom;
        }
        else {
            effectiveDate = LocalDate.now();
        }

        final List<OddsApiEvent> events = apiGateway.fetchEvents(sport, region, effectiveDate);

        if (events.isEmpty()) {
            presenter.presentFetchSuccess(new FetchGamesResponseModel(
                0, 0, sport, "No events found for the specified criteria."
            ));
            return;
        }

        // Step 2: Convert API events to Game entities
        final List<Game> games = responseAdapter.convertToGames(events);

        if (games.isEmpty()) {
            presenter.presentFetchSuccess(new FetchGamesResponseModel(
                events.size(), 0, sport, "Fetched events but none could be converted to games."
            ));
            return;
        }

        // Step 3: Normalize and validate game data
        final List<Game> validGames = normalizeAndValidateGames(games);
        gameRepository.upsertGames(validGames);
        final String message = String.format("Successfully fetched and saved %d games.", validGames.size());
        presenter.presentFetchSuccess(new FetchGamesResponseModel(
            events.size(), validGames.size(), sport, message
        ));
    }

    @Override
    public void refreshGames() {
        fetchAndUpdateGames(DEFAULT_SPORT, DEFAULT_REGION, LocalDate.now());
    }

    @Override
    public void searchGames(final String query) {
        try {
            final List<Game> games = gameRepository.searchGames(query);
            presenter.presentSearchResults(games, query);
        }
        catch (final RepositoryException ex) {
            presenter.presentFetchError("Search failed: " + ex.getMessage());
        }
    }

    /**
     * Normalizes and validates game data before saving.
     * Filters out invalid games and ensures data consistency.
     *
     * @param games The list of games to validate
     * @return List of valid games
     */
    private List<Game> normalizeAndValidateGames(final List<Game> games) {
        final List<Game> validGames = new ArrayList<>();

        for (final Game game : games) {
            if (isValidGame(game) && isFutureGame(game)) {
                validGames.add(game);
            }
        }

        return validGames;
    }

    /**
     * Validates if a game has all required fields.
     *
     * @param game The game to validate
     * @return true if game is valid, false otherwise
     */
    private boolean isValidGame(final Game game) {
        return game.getId() != null
                && game.getMarketId() != null
                && game.getGameTime() != null
                && game.getTeamA() != null
                && !game.getTeamA().trim().isEmpty()
                && game.getTeamB() != null
                && !game.getTeamB().trim().isEmpty()
                && game.getSport() != null
                && !game.getSport().trim().isEmpty();
    }

    /**
     * Checks if a game is in the future.
     *
     * @param game The game to check
     * @return true if game time is in the future, false otherwise
     */
    private boolean isFutureGame(final Game game) {
        return !game.getGameTime().isBefore(java.time.LocalDateTime.now());
    }
}

