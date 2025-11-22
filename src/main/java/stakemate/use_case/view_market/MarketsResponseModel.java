package stakemate.use_case.view_market;

import java.util.List;

public class MarketsResponseModel {
    private final String matchId;
    private final String matchTitle;
    private final List<MarketSummary> markets;
    private final String emptyStateMessage;

    public MarketsResponseModel(final String matchId,
                                final String matchTitle,
                                final List<MarketSummary> markets,
                                final String emptyStateMessage) {
        this.matchId = matchId;
        this.matchTitle = matchTitle;
        this.markets = markets;
        this.emptyStateMessage = emptyStateMessage;
    }

    public String getMatchId() {
        return matchId;
    }

    public String getMatchTitle() {
        return matchTitle;
    }

    public List<MarketSummary> getMarkets() {
        return markets;
    }

    public String getEmptyStateMessage() {
        return emptyStateMessage;
    }
}
