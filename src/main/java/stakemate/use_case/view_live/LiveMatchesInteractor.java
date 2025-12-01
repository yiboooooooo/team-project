package stakemate.use_case.view_live;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import stakemate.entity.Game;
import stakemate.use_case.fetch_games.FetchGamesInputBoundary;
import stakemate.use_case.fetch_games.GameRepository;
import stakemate.use_case.fetch_games.RepositoryException;

/**
 * Interactor for the Live Matches use case.
 * Orchestrates the periodic fetching and retrieval of game data.
 *
 * <p>
 *
 */
public class LiveMatchesInteractor implements LiveMatchesInputBoundary {

    private static final int POLLING_INTERVAL_SECONDS = 30;

    private final FetchGamesInputBoundary fetchGamesInteractor;
    private final GameRepository gameRepository;
    private final LiveMatchesOutputBoundary presenter;

    private ScheduledExecutorService scheduler;

    public LiveMatchesInteractor(final FetchGamesInputBoundary fetchGamesInteractor,
                                 final GameRepository gameRepository,
                                 final LiveMatchesOutputBoundary presenter) {
        this.fetchGamesInteractor = fetchGamesInteractor;
        this.gameRepository = gameRepository;
        this.presenter = presenter;
    }

    @Override
    public void startTracking() {
        if (shouldNotStartTracking()) {
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::fetchAndPresent, 0, POLLING_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Checks if tracking should not be started.
     *
     * @return true if scheduler is already running, false otherwise
     */
    private boolean shouldNotStartTracking() {
        return scheduler != null && !scheduler.isShutdown();
    }

    @Override
    public void stopTracking() {
        if (scheduler != null) {
            scheduler.shutdown();
            scheduler = null;
        }
    }

    private void fetchAndPresent() {
        try {
            // 1. Trigger a refresh from the API
            // Note: fetchGamesInteractor.refreshGames() usually calls its own presenter.
            // We might want to suppress that or just accept it logs to console.
            // For this use case, we primarily care about the side effect: DB update.
            fetchGamesInteractor.refreshGames();

            // 2. Query the DB for the latest data
            final List<Game> games = gameRepository.findFutureGames();

            // 3. Present the data
            presenter.presentMatches(games);
        }
        catch (final RepositoryException ex) {
            presenter.presentError("Failed to retrieve matches: " + ex.getMessage());
        }
        catch (final RuntimeException ex) {
            presenter.presentError("Unexpected error during tracking: " + ex.getMessage());
        }
    }
}
