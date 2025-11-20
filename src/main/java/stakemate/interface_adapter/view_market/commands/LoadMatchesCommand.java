package stakemate.interface_adapter.view_market.commands;

import stakemate.use_case.view_market.ViewMarketInputBoundary;

public class LoadMatchesCommand implements ViewMarketCommand {
    private final ViewMarketInputBoundary interactor;

    public LoadMatchesCommand(final ViewMarketInputBoundary interactor) {
        this.interactor = interactor;
    }

    @Override
    public void execute() {
        interactor.loadMatches();
    }
}
