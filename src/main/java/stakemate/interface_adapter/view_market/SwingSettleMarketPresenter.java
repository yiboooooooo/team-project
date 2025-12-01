package stakemate.interface_adapter.view_market;

import javax.swing.SwingUtilities;

import stakemate.use_case.settle_market.SettleMarketOutputBoundary;
import stakemate.use_case.settle_market.SettleMarketResponseModel;

public class SwingSettleMarketPresenter implements SettleMarketOutputBoundary {

    private final SettleMarketView view;

    public SwingSettleMarketPresenter(final SettleMarketView view) {
        this.view = view;
    }

    private void runOnEdt(final Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        }
        else {
            SwingUtilities.invokeLater(r);
        }
    }

    @Override
    public void presentSuccess(final SettleMarketResponseModel response) {
        final String msg = String.format(
//            "Settled market %s\nTotal Payout: %.2f\n\n%s",
//            response.getMarketId(),
//            response.getTotalPayout(),
            "Settlement Complete.\n\n%s",
            response.getSettlementSummary() // <--- Show the list of winners/losers
        );

        runOnEdt(() -> view.showSettlementResult(msg));
    }

    @Override
    public void presentFailure(final String errorMessage) {
        runOnEdt(() -> view.showSettlementError(errorMessage));
    }
}
