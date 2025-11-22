package stakemate.interface_adapter.view_market;

import stakemate.interface_adapter.view_market.commands.LoadMatchesCommand;
import stakemate.interface_adapter.view_market.commands.SelectMatchCommand;
import stakemate.interface_adapter.view_market.commands.ViewMarketCommand;
import stakemate.use_case.view_market.MarketSummary;
import stakemate.use_case.view_market.MatchSummary;
import stakemate.use_case.view_market.ViewMarketInputBoundary;

/**
 * [Command Pattern]
 * Controller creates and executes Commands instead of calling Interactor directly.
 */
public class ViewMarketController {

    private final ViewMarketInputBoundary inputBoundary;

    public ViewMarketController(final ViewMarketInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    public void refresh() {
        // Create command
        final ViewMarketCommand command = new LoadMatchesCommand(inputBoundary);
        command.execute();
    }

    public void refreshWithApi() {
        inputBoundary.refreshFromApi();
    }

    public void onMatchSelected(final MatchSummary matchSummary) {
        if (matchSummary != null) {
            final ViewMarketCommand command = new SelectMatchCommand(inputBoundary, matchSummary.getId());
            command.execute();
        }
    }

    public void onMarketSelected(final MarketSummary marketSummary) {
        if (marketSummary != null) {
            // Direct call or Command, staying consistent
            inputBoundary.marketSelected(marketSummary.getId());
        }
    }
}
