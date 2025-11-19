package stakemate.data_access.in_memory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import stakemate.entity.Game;
import stakemate.entity.GameStatus;
import stakemate.entity.Match;
import stakemate.entity.MatchStatus;
import stakemate.use_case.view_market.MatchRepository;
import stakemate.use_case.view_market.RepositoryException;
import stakemate.use_case.fetch_games.GameRepository;
import stakemate.use_case.fetch_games.FetchGamesInputBoundary;

public class InMemoryMatchRepository implements MatchRepository {

    private final List<Match> matches = new ArrayList<>();
    private final GameRepository gameRepository;
    private final FetchGamesInputBoundary fetchGamesInteractor;

    public InMemoryMatchRepository() {
        this(null, null);
    }

    public InMemoryMatchRepository(GameRepository gameRepository) {
        this(gameRepository, null);
    }

    public InMemoryMatchRepository(GameRepository gameRepository, FetchGamesInputBoundary fetchGamesInteractor) {
        this.gameRepository = gameRepository;
        this.fetchGamesInteractor = fetchGamesInteractor;
    }

    private void initializeWithDefaultMatches() {
        LocalDateTime now = LocalDateTime.now();

        matches.add(new Match("M1", "Raptors", "Lakers",
                MatchStatus.UPCOMING, now.plusHours(2)));
        matches.add(new Match("M2", "Celtics", "Bulls",
                MatchStatus.LIVE, now.minusMinutes(30)));
        matches.add(new Match("M3", "Warriors", "Nets",
                MatchStatus.CLOSED, now.minusHours(4)));
    }

    @Override
    public List<Match> findAllMatches() throws RepositoryException {
        return new ArrayList<>(matches);
    }

    /**
     * Syncs matches with latest games from the API/database.
     * This method fetches games from API, saves to DB, then converts to Match objects.
     */
    public void syncWithApiData() throws RepositoryException {
        if (gameRepository == null) {
            // No game repository configured, use default matches
            initializeWithDefaultMatches();
            return;
        }

        try {
            // Step 1: Fetch fresh data from API if interactor is available
            if (fetchGamesInteractor != null) {
                fetchGamesInteractor.refreshGames(); // This calls API and saves to DB
            }
            
            // Step 2: Read the updated games from database
            List<Game> games = gameRepository.searchGames(""); // Get all games
            List<Match> apiMatches = convertGamesToMatches(games);
            
            // Step 3: Replace current matches with API data
            matches.clear();
            matches.addAll(apiMatches);
            
            // If no API matches found, fall back to default matches
            if (matches.isEmpty()) {
                initializeWithDefaultMatches();
            }
        } catch (Exception e) {
            // Fall back to default matches on error
            initializeWithDefaultMatches();
            throw new RepositoryException("Failed to sync with API data: " + e.getMessage(), e);
        }
    }

    /**
     * Converts Game entities to Match entities.
     * This is the Game-to-Match adapter functionality.
     */
    private List<Match> convertGamesToMatches(List<Game> games) {
        List<Match> matchList = new ArrayList<>();
        
        for (Game game : games) {
            Match match = convertGameToMatch(game);
            if (match != null) {
                matchList.add(match);
            }
        }
        
        return matchList;
    }

    /**
     * Converts a single Game to a Match.
     */
    private Match convertGameToMatch(Game game) {
        if (game == null) {
            return null;
        }

        // Use external ID consistently as match ID to prevent duplicates
        String matchId = game.getExternalId() != null ? game.getExternalId() : game.getId().toString();
        
        // Convert GameStatus to MatchStatus
        MatchStatus matchStatus = convertGameStatusToMatchStatus(game.getStatus());
        
        return new Match(
            matchId,
            game.getTeamA(),  // home team
            game.getTeamB(),  // away team
            matchStatus,
            game.getGameTime()
        );
    }

    /**
     * Maps GameStatus enum values to MatchStatus enum values.
     */
    private MatchStatus convertGameStatusToMatchStatus(GameStatus gameStatus) {
        if (gameStatus == null) {
            return MatchStatus.UPCOMING;
        }
        
        switch (gameStatus) {
            case UPCOMING:
                return MatchStatus.UPCOMING;
            case LIVE:
                return MatchStatus.LIVE;
            case FINISHED:
                return MatchStatus.CLOSED;
            default:
                return MatchStatus.UPCOMING;
        }
    }
}
