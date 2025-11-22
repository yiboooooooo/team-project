package stakemate.interface_adapter.controllers;


import stakemate.use_case.settle_market.SettleMarketInputBoundary;
import stakemate.use_case.settle_market.SettleMarketRequestModel;

public class SettleMarketController {

    private final SettleMarketInputBoundary interactor;

    public SettleMarketController(final SettleMarketInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void settleMarket(final String marketId, final boolean homeTeamWon) {
        final SettleMarketRequestModel request = new SettleMarketRequestModel(marketId, homeTeamWon);
        interactor.execute(request);
    }
}
