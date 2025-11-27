package stakemate.use_case.view_market.builder;

import java.util.ArrayList;
import java.util.List;

import stakemate.use_case.view_market.MarketSummary;
import stakemate.use_case.view_market.MarketsResponseModel;

/**
 * [Builder Pattern]
 * Constructs complex MarketsResponseModel objects step-by-step.
 */
public class MarketsResponseModelBuilder {
    private String matchId;
    private String matchTitle;
    private List<MarketSummary> markets = new ArrayList<>();
    private String emptyStateMessage;

    /**
     * Sets the ID of the match associated with these markets.
     *
     * @param matchId the match ID.
     * @return the builder instance.
     */
    public MarketsResponseModelBuilder setMatchId(final String matchId) {
        this.matchId = matchId;
        return this;
    }

    /**
     * Sets the display title of the match (e.g., "Team A vs Team B").
     *
     * @param matchTitle the match title.
     * @return the builder instance.
     */
    public MarketsResponseModelBuilder setMatchTitle(final String matchTitle) {
        this.matchTitle = matchTitle;
        return this;
    }

    /**
     * Adds a single market summary to the list of markets.
     *
     * @param market the market summary to add.
     * @return the builder instance.
     */
    public MarketsResponseModelBuilder addMarket(final MarketSummary market) {
        this.markets.add(market);
        return this;
    }

    /**
     * Sets the entire list of market summaries, replacing any existing ones.
     *
     * @param markets the list of market summaries.
     * @return the builder instance.
     */
    public MarketsResponseModelBuilder setMarkets(final List<MarketSummary> markets) {
        this.markets = markets;
        return this;
    }

    /**
     * Sets the message to display if the list of markets is empty.
     *
     * @param msg the empty state message.
     * @return the builder instance.
     */
    public MarketsResponseModelBuilder setEmptyStateMessage(final String msg) {
        this.emptyStateMessage = msg;
        return this;
    }

    /**
     * Constructs the final MarketsResponseModel.
     * If no markets were added and no empty message was set, a default message is applied.
     *
     * @return the constructed MarketsResponseModel.
     */
    public MarketsResponseModel build() {
        if (emptyStateMessage == null && markets.isEmpty()) {
            emptyStateMessage = "No markets available.";
        }
        return new MarketsResponseModel(matchId, matchTitle, markets, emptyStateMessage);
    }
}
