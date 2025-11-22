package stakemate.interface_adapter.fetch_games;

import java.util.List;

import javax.swing.SwingUtilities;

import stakemate.entity.Game;
import stakemate.use_case.fetch_games.FetchGamesOutputBoundary;
import stakemate.use_case.fetch_games.FetchGamesResponseModel;

/**
 * Presenter for FetchGames use case using Swing.
 * Formats use case output for display in the UI.
 */
public class SwingFetchGamesPresenter implements FetchGamesOutputBoundary {

    private final FetchGamesView view;

    public SwingFetchGamesPresenter(final FetchGamesView view) {
        this.view = view;
    }

    private void runOnEdt(final Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        }
        else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    @Override
    public void presentFetchSuccess(final FetchGamesResponseModel responseModel) {
        runOnEdt(() -> {
            final String message = responseModel.getMessage() != null
                ? responseModel.getMessage()
                : String.format("Fetched %d games, saved %d games",
                responseModel.getGamesFetched(),
                responseModel.getGamesSaved());
            view.showFetchSuccess(message, responseModel.getGamesSaved());
        });
    }

    @Override
    public void presentFetchError(final String errorMessage) {
        runOnEdt(() -> view.showError(errorMessage));
    }

    @Override
    public void presentFetchInProgress() {
        runOnEdt(() -> view.showFetchInProgress());
    }

    @Override
    public void presentSearchResults(final List<Game> games, final String query) {
        runOnEdt(() -> view.showSearchResults(games, query));
    }
}

