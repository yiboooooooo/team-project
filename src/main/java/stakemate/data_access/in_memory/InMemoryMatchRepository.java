package stakemate.data_access.in_memory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stakemate.entity.Game;
import stakemate.entity.GameStatus;
import stakemate.entity.Match;
import stakemate.entity.MatchStatus;
import stakemate.use_case.fetch_games.FetchGamesInputBoundary;
import stakemate.use_case.fetch_games.GameRepository;
import stakemate.use_case.view_market.MatchRepository;
import stakemate.use_case.view_market.RepositoryException;

public class InMemoryMatchRepository implements MatchRepository {

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
            MatchStatus.UPCOMING, now.plusHours(2)));
        matches.add(new Match("M2", "Celtics", "Bulls",
            MatchStatus.LIVE, now.minusMinutes(30)));
        matches.add(new Match("M3", "Warriors", "Nets",
            MatchStatus.CLOSED, now.minusHours(4)));
    }

    /**
     * Retrieves all matches from the repository.
     *
     * @return a list of all matches
     * @throws RepositoryException if retrieval fails
     */
    @Override
    public List<Match> findAllMatches() throws RepositoryException {
        return new ArrayList<>(matches);
    }

    /**
     * Syncs matches with latest games from the API/database.
     * This method fetches games from API, saves to DB, then converts to Match objects.
     * Merges new matches with existing ones instead of replacing all matches.
     *
     * @throws RepositoryException if sync fails
     */
    public void syncWithApiData() throws RepositoryException {
        if (gameRepository == null) {
            // No game repository configured, use default matches
            initializeWithDefaultMatches();
        }

        try {
            // Step 1: Fetch fresh data from API if interactor is available
            if (fetchGamesInteractor != null) {
                fetchGamesInteractor.refreshGames();
            }

            // Step 2: Read all games from database (includes both old and new)
            final List<Game> games = gameRepository.searchGames("");
            final List<Match> newMatches = convertGamesToMatches(games);

            // Step 3: Merge new matches with existing ones
            // Use a map to track matches by ID to avoid duplicates
            final Map<String, Match> matchMap = new HashMap<>();

            // First, add all existing matches
            for (final Match existingMatch : matches) {
                matchMap.put(existingMatch.getId(), existingMatch);
            }

            // Then, update or add new matches (this will update existing or add new)
            for (final Match newMatch : newMatches) {
                matchMap.put(newMatch.getId(), newMatch);
            }

            // Replace matches list with merged results
            matches.clear();
            matches.addAll(matchMap.values());

            if (matches.isEmpty()) {
                initializeWithDefaultMatches();
            }
        }
        catch (final RepositoryException ex) {
            // Fall back to default matches on error
            initializeWithDefaultMatches();
            throw new RepositoryException("Failed to sync with API data: " + ex.getMessage(), ex);
        }
    }

    /**
     * Converts Game entities to Match entities.
     * This is the Game-to-Match adapter functionality.
     * No filtering applied - all games are converted with their actual status.
     *
     * @param games the list of games to convert
     * @return the list of converted matches
     */
    private List<Match> convertGamesToMatches(final List<Game> games) {
        final List<Match> matchList = new ArrayList<>();

        for (final Game game : games) {
            // Skip games with null game time
            if (game.getGameTime() == null) {
                continue;
            }

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
     * @param game the game to convert
     * @return the converted match, or null if game is null
     */
    private Match convertGameToMatch(final Game game) {
        Match res;
        if (game == null) {
            res = null;
        }

        // Use external ID consistently as match ID to prevent duplicates
        final String matchId;
        if (game.getExternalId() != null) {
            matchId = game.getExternalId();
        }
        else {
            matchId = game.getId().toString();
        }
        final MatchStatus matchStatus = convertGameStatusToMatchStatus(game.getStatus());

        res = new Match(
            matchId,
            /* TeamA = home team, TeamB = away team */
            game.getTeamA(),
            game.getTeamB(),
            matchStatus,
            game.getGameTime()
        );
        return res;
    }

    /**
     * Maps GameStatus enum values to MatchStatus enum values.
     *
     * @param gameStatus the game status to convert
     * @return the corresponding match status
     */
    private MatchStatus convertGameStatusToMatchStatus(final GameStatus gameStatus) {
        MatchStatus res;
        if (gameStatus == null) {
            res = MatchStatus.UPCOMING;
        }

        switch (gameStatus) {
            case UPCOMING:
                res = MatchStatus.UPCOMING;
                break;
            case LIVE:
                res = MatchStatus.LIVE;
                break;
            case FINISHED:
                res = MatchStatus.CLOSED;
                break;
            default:
                res = MatchStatus.UPCOMING;
                break;
        }
        return res;
    }
}
