package stakemate.interface_adapter.view_market;

import stakemate.use_case.settle_market.SettleMarketOutputBoundary;
import stakemate.use_case.settle_market.SettleMarketResponseModel;

import javax.swing.*;

public class SwingSettleMarketPresenter implements SettleMarketOutputBoundary {

    private final SettleMarketView view;

    public SwingSettleMarketPresenter(SettleMarketView view) {
        this.view = view;
    }

    private void runOnEdt(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    @Override
    public void presentSuccess(SettleMarketResponseModel response) {
        String msg = String.format(
                "Settled market %s: %d bets, total payout %.2f",
                response.getMarketId(),
                response.getBetsSettled(),
                response.getTotalPayout()
        );

        runOnEdt(() -> view.showSettlementResult(msg));
    }

    @Override
    public void presentFailure(String errorMessage) {
        runOnEdt(() -> view.showSettlementError(errorMessage));
    }
}
