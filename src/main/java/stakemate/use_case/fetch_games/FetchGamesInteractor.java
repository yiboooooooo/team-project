package stakemate.use_case.fetch_games;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import stakemate.data_access.api.OddsApiResponseAdapter;
import stakemate.entity.Game;

/**
 * Interactor for the FetchGames use case.
 * Orchestrates the flow: API -> Adapter -> Repository -> Presenter
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
            // Validate sport parameter
            if (sport == null || sport.trim().isEmpty()) {
                presenter.presentFetchError("Sport parameter is required");
                return;
            }

            // Use today if dateFrom is null
            final LocalDate effectiveDate = dateFrom != null ? dateFrom : LocalDate.now();
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
        catch (final ApiException e) {
            presenter.presentFetchError("API error: " + e.getMessage());
        }
        catch (final RepositoryException e) {
            presenter.presentFetchError("Database error: " + e.getMessage());
        }
        catch (final Exception e) {
            presenter.presentFetchError("Unexpected error: " + e.getMessage());
        }
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
        catch (final RepositoryException e) {
            presenter.presentFetchError("Search failed: " + e.getMessage());
        }
    }

    /**
     * Normalizes and validates game data before saving.
     * Filters out invalid games and ensures data consistency.
     */
    private List<Game> normalizeAndValidateGames(final List<Game> games) {
        final List<Game> validGames = new ArrayList<>();

        for (final Game game : games) {
            // Validate required fields
            if (game.getId() == null ||
                game.getMarketId() == null ||
                game.getGameTime() == null ||
                game.getTeamA() == null || game.getTeamA().trim().isEmpty() ||
                game.getTeamB() == null || game.getTeamB().trim().isEmpty() ||
                game.getSport() == null || game.getSport().trim().isEmpty()) {
                // Skip invalid games
                continue;
            }

            // Only include future games (game_time >= today)
            if (game.getGameTime().isBefore(java.time.LocalDateTime.now())) {
                // Skip past games
                continue;
            }

            validGames.add(game);
        }

        return validGames;
    }
}

