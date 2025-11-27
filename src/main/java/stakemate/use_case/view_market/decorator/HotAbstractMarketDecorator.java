package stakemate.use_case.view_market.decorator;

import stakemate.use_case.view_market.MarketSummary;

public class HotAbstractMarketDecorator extends AbstractMarketSummaryDecorator {
    public HotAbstractMarketDecorator(final MarketSummary wrapped) {
        super(wrapped);
    }

    @Override
    public String toString() {
        // Adds functionality (visual flare) to the existing object
        return super.toString() + " [HOT]";
    }
}
