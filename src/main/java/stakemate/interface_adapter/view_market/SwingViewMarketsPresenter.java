package stakemate.interface_adapter.view_market;

import javax.swing.SwingUtilities;

import stakemate.use_case.view_market.MarketsResponseModel;
import stakemate.use_case.view_market.MatchesResponseModel;
import stakemate.use_case.view_market.OrderBookResponseModel;
import stakemate.use_case.view_market.ViewMarketOutputBoundary;

public class SwingViewMarketsPresenter implements ViewMarketOutputBoundary {

    private final MarketsView view;

    public SwingViewMarketsPresenter(final MarketsView view) {
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
    public void presentMatches(final MatchesResponseModel responseModel) {
        runOnEdt(() ->
            view.showMatches(responseModel.getMatches(), responseModel.getEmptyStateMessage())
        );
    }

    @Override
    public void presentMarketsForMatch(final MarketsResponseModel responseModel) {
        runOnEdt(() -> view.showMarketsForMatch(responseModel));
    }

    @Override
    public void presentOrderBook(final OrderBookResponseModel responseModel) {
        runOnEdt(() -> view.showOrderBook(responseModel));
    }

    @Override
    public void presentError(final String userMessage) {
        runOnEdt(() -> view.showError(userMessage));
    }
}
