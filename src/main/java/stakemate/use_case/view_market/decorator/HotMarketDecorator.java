package stakemate.use_case.view_market.decorator;

import stakemate.use_case.view_market.MarketSummary;

public class HotMarketDecorator extends MarketSummaryDecorator {
    public HotMarketDecorator(MarketSummary wrapped) {
        super(wrapped);
    }

    @Override
    public String toString() {
        // Adds functionality (visual flare) to the existing object
        return super.toString() + " 🔥 [HOT]";
    }
}
