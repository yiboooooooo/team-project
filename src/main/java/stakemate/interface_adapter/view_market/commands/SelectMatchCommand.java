package stakemate.interface_adapter.view_market.commands;

import stakemate.use_case.view_market.ViewMarketInputBoundary;

public class SelectMatchCommand implements ViewMarketCommand {
    private final ViewMarketInputBoundary interactor;
    private final String matchId;

    public SelectMatchCommand(final ViewMarketInputBoundary interactor, final String matchId) {
        this.interactor = interactor;
        this.matchId = matchId;
    }

    @Override
    public void execute() {
        interactor.matchSelected(matchId);
    }
}
