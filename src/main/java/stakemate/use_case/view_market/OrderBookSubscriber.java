package stakemate.use_case.view_market;

import stakemate.entity.OrderBook;

public interface OrderBookSubscriber {
    void onOrderBookUpdated(OrderBook orderBook);

    void onConnectionError(String message);

    void onConnectionRestored();
}
