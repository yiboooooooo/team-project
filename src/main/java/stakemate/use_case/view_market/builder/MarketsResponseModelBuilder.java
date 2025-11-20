package stakemate.use_case.view_market.builder;

import stakemate.use_case.view_market.MarketSummary;
import stakemate.use_case.view_market.MarketsResponseModel;

import java.util.ArrayList;
import java.util.List;

/**
 * [Builder Pattern]
 * Constructs complex MarketsResponseModel objects step-by-step.
 */
public class MarketsResponseModelBuilder {
    private String matchId;
    private String matchTitle;
    private List<MarketSummary> markets = new ArrayList<>();
    private String emptyStateMessage;

    public MarketsResponseModelBuilder setMatchId(String matchId) {
        this.matchId = matchId;
        return this;
    }

    public MarketsResponseModelBuilder setMatchTitle(String matchTitle) {
        this.matchTitle = matchTitle;
        return this;
    }

    public MarketsResponseModelBuilder addMarket(MarketSummary market) {
        this.markets.add(market);
        return this;
    }

    public MarketsResponseModelBuilder setMarkets(List<MarketSummary> markets) {
        this.markets = markets;
        return this;
    }

    public MarketsResponseModelBuilder setEmptyStateMessage(String msg) {
        this.emptyStateMessage = msg;
        return this;
    }

    public MarketsResponseModel build() {
        if (emptyStateMessage == null && markets.isEmpty()) {
            emptyStateMessage = "No markets available.";
        }
        return new MarketsResponseModel(matchId, matchTitle, markets, emptyStateMessage);
    }
}
