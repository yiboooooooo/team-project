package stakemate.use_case.view_market;

import stakemate.entity.OrderBook;

public interface OrderBookSubscriber {
    /**
     * Called when the order book data has been updated.
     *
     * @param orderBook the new snapshot of the order book.
     */
    void onOrderBookUpdated(OrderBook orderBook);

    /**
     * Called when there is an error connecting to the order book data source.
     *
     * @param message a description of the connection error.
     */
    void onConnectionError(String message);

    /**
     * Called when the connection to the data source has been successfully restored.
     */
    void onConnectionRestored();
}
