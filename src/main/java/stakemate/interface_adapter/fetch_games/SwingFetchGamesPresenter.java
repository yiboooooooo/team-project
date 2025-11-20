package stakemate.interface_adapter.fetch_games;

import stakemate.entity.Game;
import stakemate.use_case.fetch_games.FetchGamesOutputBoundary;
import stakemate.use_case.fetch_games.FetchGamesResponseModel;

import javax.swing.*;
import java.util.List;

/**
 * Presenter for FetchGames use case using Swing.
 * Formats use case output for display in the UI.
 */
public class SwingFetchGamesPresenter implements FetchGamesOutputBoundary {

    private final FetchGamesView view;

    public SwingFetchGamesPresenter(FetchGamesView view) {
        this.view = view;
    }

    private void runOnEdt(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    @Override
    public void presentFetchSuccess(FetchGamesResponseModel responseModel) {
        runOnEdt(() -> {
            String message = responseModel.getMessage() != null
                ? responseModel.getMessage()
                : String.format("Fetched %d games, saved %d games",
                responseModel.getGamesFetched(),
                responseModel.getGamesSaved());
            view.showFetchSuccess(message, responseModel.getGamesSaved());
        });
    }

    @Override
    public void presentFetchError(String errorMessage) {
        runOnEdt(() -> view.showError(errorMessage));
    }

    @Override
    public void presentFetchInProgress() {
        runOnEdt(() -> view.showFetchInProgress());
    }

    @Override
    public void presentSearchResults(List<Game> games, String query) {
        runOnEdt(() -> view.showSearchResults(games, query));
    }
}

