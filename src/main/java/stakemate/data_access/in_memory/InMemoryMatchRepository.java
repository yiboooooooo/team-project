package stakemate.data_access.in_memory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import stakemate.entity.Game;
import stakemate.entity.GameStatus;
import stakemate.entity.Match;
import stakemate.entity.MatchStatus;
import stakemate.use_case.fetch_games.FetchGamesInputBoundary;
import stakemate.use_case.fetch_games.GameRepository;
import stakemate.use_case.view_market.MatchRepository;
import stakemate.use_case.view_market.RepositoryException;

public class InMemoryMatchRepository implements MatchRepository {

    private static final int DEFAULT_OFFSET_HOURS_UPCOMING = 2;
    private static final int DEFAULT_OFFSET_MINUTES_LIVE = 30;
    private static final int DEFAULT_OFFSET_HOURS_CLOSED = 4;

    private final List<Match> matches = new ArrayList<>();
    private final GameRepository gameRepository;
    private final FetchGamesInputBoundary fetchGamesInteractor;

    public InMemoryMatchRepository() {
        this(null, null);
    }

    public InMemoryMatchRepository(final GameRepository gameRepository) {
        this(gameRepository, null);
    }

    public InMemoryMatchRepository(final GameRepository gameRepository,
                                   final FetchGamesInputBoundary fetchGamesInteractor) {
        this.gameRepository = gameRepository;
        this.fetchGamesInteractor = fetchGamesInteractor;

        // Attempt to load data immediately upon creation
        try {
            syncWithApiData();
        }
        catch (final RepositoryException ex) {
            System.err.println("InMemoryMatchRepository startup sync failed: " + ex.getMessage());
            initializeWithDefaultMatches();
        }
    }

    private void initializeWithDefaultMatches() {
        final LocalDateTime now = LocalDateTime.now();

        matches.add(new Match("M1", "Raptors", "Lakers",
            MatchStatus.UPCOMING, now.plusHours(DEFAULT_OFFSET_HOURS_UPCOMING)));
        matches.add(new Match("M2", "Celtics", "Bulls",
            MatchStatus.LIVE, now.minusMinutes(DEFAULT_OFFSET_MINUTES_LIVE)));
        matches.add(new Match("M3", "Warriors", "Nets",
            MatchStatus.CLOSED, now.minusHours(DEFAULT_OFFSET_HOURS_CLOSED)));
    }

    @Override
    public List<Match> findAllMatches() throws RepositoryException {
        return new ArrayList<>(matches);
    }

    /**
     * Syncs matches with latest games from the API/database.
     * This method fetches games from API, saves to DB, then converts to Match objects.
     *
     * @throws RepositoryException if the data cannot be fetched or saved.
     */
    public void syncWithApiData() throws RepositoryException {
        if (gameRepository == null) {
            // No game repository configured, use default matches
            initializeWithDefaultMatches();
        }
        else {
            try {
                // Fetch fresh data from API if interactor is available
                if (fetchGamesInteractor != null) {
                    fetchGamesInteractor.refreshGames();
                }

                // Read the updated games from database
                // This throws stakemate.use_case.fetch_games.RepositoryException
                final List<Game> games = gameRepository.searchGames("");
                final List<Match> apiMatches = convertGamesToMatches(games);
                matches.clear();
                matches.addAll(apiMatches);
                if (matches.isEmpty()) {
                    initializeWithDefaultMatches();
                }
            }
            // Explicitly catch the exception from the fetch_games package
            catch (final stakemate.use_case.fetch_games.RepositoryException ex) {
                // Fall back to default matches on error
                initializeWithDefaultMatches();
                // Wrap and throw as view_market.RepositoryException
                throw new RepositoryException("Failed to sync with API data: " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * Converts Game entities to Match entities.
     * This is the Game-to-Match adapter functionality.
     *
     * @param games the list of Game entities.
     * @return a list of Match entities.
     */
    private List<Match> convertGamesToMatches(final List<Game> games) {
        final List<Match> matchList = new ArrayList<>();

        for (final Game game : games) {
            final Match match = convertGameToMatch(game);
            if (match != null) {
                matchList.add(match);
            }
        }

        return matchList;
    }

    /**
     * Converts a single Game to a Match.
     *
     * @param game the Game entity to convert.
     * @return the corresponding Match entity, or null if game is null.
     */
    private Match convertGameToMatch(final Game game) {
        final Match match;
        if (game == null) {
            match = null;
        }
        else {
            // Use external ID consistently as match ID to prevent duplicates
            final String matchId;
            if (game.getExternalId() != null) {
                matchId = game.getExternalId();
            }
            else {
                matchId = game.getId().toString();
            }

            final MatchStatus matchStatus = convertGameStatusToMatchStatus(game.getStatus());

            match = new Match(
                matchId,
                game.getTeamA(),
                game.getTeamB(),
                matchStatus,
                game.getGameTime()
            );
        }
        return match;
    }

    /**
     * Maps GameStatus enum values to MatchStatus enum values.
     *
     * @param gameStatus the GameStatus to convert.
     * @return the corresponding MatchStatus.
     */
    private MatchStatus convertGameStatusToMatchStatus(final GameStatus gameStatus) {
        final MatchStatus result;
        if (gameStatus == null) {
            result = MatchStatus.UPCOMING;
        }
        else {
            switch (gameStatus) {
                case UPCOMING:
                    result = MatchStatus.UPCOMING;
                    break;
                case LIVE:
                    result = MatchStatus.LIVE;
                    break;
                case FINISHED:
                    result = MatchStatus.CLOSED;
                    break;
                default:
                    result = MatchStatus.UPCOMING;
                    break;
            }
        }
        return result;
    }
}
