package stakemate.engine;

import stakemate.entity.OrderBook;

/**
 * Small helper — left for clarity. MatchingEngine::snapshotOrderBook already returns an OrderBook.
 */
public final class OrderBookFactory {
    private OrderBookFactory() {
    }

    public static OrderBook of(MatchingEngine engine, String marketId) {
        return engine.snapshotOrderBook(marketId);
    }
}
