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

    public ViewMarketController(ViewMarketInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    public void refresh() {
        // Create command
        ViewMarketCommand command = new LoadMatchesCommand(inputBoundary);
        // Execute
        command.execute();
    }

    public void refreshWithApi() {
        inputBoundary.refreshFromApi(); // Could be wrapped in Command too
    }

    public void onMatchSelected(MatchSummary matchSummary) {
        if (matchSummary != null) {
            ViewMarketCommand command = new SelectMatchCommand(inputBoundary, matchSummary.getId());
            command.execute();
        }
    }

    public void onMarketSelected(MarketSummary marketSummary) {
        if (marketSummary != null) {
            // Direct call or Command, staying consistent
            inputBoundary.marketSelected(marketSummary.getId());
        }
    }
}
