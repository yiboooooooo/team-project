package stakemate.use_case.view_market;

import stakemate.entity.OrderBook;

public class OrderBookResponseModel {
    private final OrderBook orderBook; // may be null on reconnecting
    private final boolean empty;
    private final boolean reconnecting;
    private final String message;

    public OrderBookResponseModel(OrderBook orderBook,
                                  boolean empty,
                                  boolean reconnecting,
                                  String message) {
        this.orderBook = orderBook;
        this.empty = empty;
        this.reconnecting = reconnecting;
        this.message = message;
    }

    public OrderBook getOrderBook() {
        return orderBook;
    }

    public boolean isEmpty() {
        return empty;
    }

    public boolean isReconnecting() {
        return reconnecting;
    }

    public String getMessage() {
        return message;
    }
}
