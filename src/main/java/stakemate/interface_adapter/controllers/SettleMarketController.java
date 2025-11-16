package stakemate.interface_adapter.controllers;


import stakemate.use_case.settle_market.*;

public class SettleMarketController {

    private final SettleMarketInputBoundary interactor;

    public SettleMarketController(SettleMarketInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void settleMarket(String marketId, boolean homeTeamWon) {
        SettleMarketRequestModel request = new SettleMarketRequestModel(marketId, homeTeamWon);
        interactor.execute(request);
    }
}