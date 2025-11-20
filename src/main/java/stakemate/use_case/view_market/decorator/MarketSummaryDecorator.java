package stakemate.use_case.view_market.decorator;

import stakemate.use_case.view_market.MarketSummary;

/**
 * [Decorator Pattern]
 * Abstract decorator for MarketSummary.
 */
public abstract class MarketSummaryDecorator extends MarketSummary {
    protected final MarketSummary wrapped;

    public MarketSummaryDecorator(MarketSummary wrapped) {
        super(wrapped.getId(), wrapped.getName(), wrapped.getStatusLabel(), wrapped.isBuySellEnabled());
        this.wrapped = wrapped;
    }

    @Override
    public String toString() {
        return wrapped.toString();
    }
}