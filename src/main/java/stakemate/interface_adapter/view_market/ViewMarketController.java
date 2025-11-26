package stakemate.interface_adapter.view_market;

import stakemate.interface_adapter.view_market.commands.LoadMatchesCommand;
import stakemate.interface_adapter.view_market.commands.SelectMatchCommand;
import stakemate.interface_adapter.view_market.commands.ViewMarketCommand;
import stakemate.use_case.view_market.MarketSummary;
import stakemate.use_case.view_market.MatchSummary;
import stakemate.use_case.view_market.ViewMarketInputBoundary;

/**
 * Command Pattern
 * Controller creates and executes Commands instead of calling Interactor directly.
 */
public class ViewMarketController {

    private final ViewMarketInputBoundary inputBoundary;

    public ViewMarketController(final ViewMarketInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    /**
     * Refreshes the list of matches by executing a LoadMatchesCommand.
     * This typically loads matches from the local database or cache.
     */
    public void refresh() {
        // Create command
        final ViewMarketCommand command = new LoadMatchesCommand(inputBoundary);
        command.execute();
    }

    /**
     * Triggers a refresh of data from the external API.
     * Delegates directly to the interactor to perform the API sync and subsequent update.
     */
    public void refreshWithApi() {
        inputBoundary.refreshFromApi();
    }

    /**
     * Handles the selection of a specific match from the view.
     * Executes a SelectMatchCommand to load markets for the selected match.
     *
     * @param matchSummary The summary object of the selected match.
     */
    public void onMatchSelected(final MatchSummary matchSummary) {
        if (matchSummary != null) {
            final ViewMarketCommand command = new SelectMatchCommand(inputBoundary, matchSummary.getId());
            command.execute();
        }
    }

    /**
     * Handles the selection of a specific market from the view.
     * Delegates to the interactor to load the order book for the selected market.
     *
     * @param marketSummary The summary object of the selected market.
     */
    public void onMarketSelected(final MarketSummary marketSummary) {
        if (marketSummary != null) {
            // Direct call or Command, staying consistent
            inputBoundary.marketSelected(marketSummary.getId());
        }
    }
}
