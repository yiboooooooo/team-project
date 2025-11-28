package stakemate.use_case.view_market.decorator;

import stakemate.use_case.view_market.MarketSummary;

/**
 * [Decorator Pattern]
 * Abstract decorator for MarketSummary.
 */
public abstract class AbstractMarketSummaryDecorator extends MarketSummary {
    private final MarketSummary wrapped;

    public AbstractMarketSummaryDecorator(final MarketSummary wrapped) {
        super(wrapped.getId(), wrapped.getName(), wrapped.getStatusLabel(), wrapped.isBuySellEnabled());
        this.wrapped = wrapped;
    }

    /**
     * Gets the wrapped MarketSummary.
     *
     * @return the wrapped object.
     */
    protected MarketSummary getWrapped() {
        return wrapped;
    }

    @Override
    public String toString() {
        return wrapped.toString();
    }
}
