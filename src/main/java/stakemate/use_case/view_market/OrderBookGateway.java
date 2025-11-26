package stakemate.use_case.view_market;

import stakemate.entity.OrderBook;

public interface OrderBookGateway {
    /**
     * Retrieves the current state of the order book for a specific market.
     *
     * @param marketId the ID of the market.
     * @return the current OrderBook snapshot.
     * @throws RepositoryException if data access fails.
     */
    OrderBook getSnapshot(String marketId) throws RepositoryException;

    /**
     * Subscribes an observer to real-time updates for a specific order book.
     *
     * @param marketId   the ID of the market to watch.
     * @param subscriber the observer to notify of updates.
     */
    void subscribe(String marketId, OrderBookSubscriber subscriber);

    /**
     * Unsubscribes an observer from updates for a specific market.
     *
     * @param marketId   the ID of the market.
     * @param subscriber the observer to remove.
     */
    void unsubscribe(String marketId, OrderBookSubscriber subscriber);
}
