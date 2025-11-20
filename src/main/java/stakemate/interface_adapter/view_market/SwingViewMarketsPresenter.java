package stakemate.interface_adapter.view_market;

import stakemate.use_case.view_market.MarketsResponseModel;
import stakemate.use_case.view_market.MatchesResponseModel;
import stakemate.use_case.view_market.OrderBookResponseModel;
import stakemate.use_case.view_market.ViewMarketOutputBoundary;

import javax.swing.*;

public class SwingViewMarketsPresenter implements ViewMarketOutputBoundary {

    private final MarketsView view;

    public SwingViewMarketsPresenter(MarketsView view) {
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
    public void presentMatches(MatchesResponseModel responseModel) {
        runOnEdt(() ->
            view.showMatches(responseModel.getMatches(), responseModel.getEmptyStateMessage())
        );
    }

    @Override
    public void presentMarketsForMatch(MarketsResponseModel responseModel) {
        runOnEdt(() -> view.showMarketsForMatch(responseModel));
    }

    @Override
    public void presentOrderBook(OrderBookResponseModel responseModel) {
        runOnEdt(() -> view.showOrderBook(responseModel));
    }

    @Override
    public void presentError(String userMessage) {
        runOnEdt(() -> view.showError(userMessage));
    }
}
