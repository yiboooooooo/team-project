package stakemate.use_case.view_market;

import stakemate.entity.OrderBook;

public interface OrderBookGateway {
    OrderBook getSnapshot(String marketId) throws RepositoryException;

    void subscribe(String marketId, OrderBookSubscriber subscriber);

    void unsubscribe(String marketId, OrderBookSubscriber subscriber);
}
