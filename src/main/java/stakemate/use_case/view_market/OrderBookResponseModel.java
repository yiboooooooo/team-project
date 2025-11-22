package stakemate.use_case.view_market;

import stakemate.entity.OrderBook;

public class OrderBookResponseModel {
    private final OrderBook orderBook;
    private final boolean empty;
    private final boolean reconnecting;
    private final String message;

    public OrderBookResponseModel(final OrderBook orderBook,
                                  final boolean empty,
                                  final boolean reconnecting,
                                  final String message) {
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
