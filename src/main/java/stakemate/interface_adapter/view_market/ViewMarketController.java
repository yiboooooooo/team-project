package stakemate.interface_adapter.view_market;

import stakemate.use_case.view_market.MatchSummary;
import stakemate.use_case.view_market.MarketSummary;
import stakemate.use_case.view_market.ViewMarketInputBoundary;

public class ViewMarketController {

    private final ViewMarketInputBoundary inputBoundary;

    public ViewMarketController(ViewMarketInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    public void refresh() {
        inputBoundary.loadMatches();
    }

    public void refreshWithApi() {
        inputBoundary.refreshFromApi();
    }

    public void onMatchSelected(MatchSummary matchSummary) {
        if (matchSummary != null) {
            inputBoundary.matchSelected(matchSummary.getId());
        }
    }

    public void onMarketSelected(MarketSummary marketSummary) {
        if (marketSummary != null) {
            inputBoundary.marketSelected(marketSummary.getId());
        }
    }
}
