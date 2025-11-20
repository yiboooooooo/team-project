package stakemate.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderBook {
    private final String marketId;
    private final List<OrderBookEntry> bids;
    private final List<OrderBookEntry> asks;

    public OrderBook(String marketId,
                     List<OrderBookEntry> bids,
                     List<OrderBookEntry> asks) {
        this.marketId = marketId;
        this.bids = Collections.unmodifiableList(new ArrayList<>(bids));
        this.asks = Collections.unmodifiableList(new ArrayList<>(asks));
    }

    public String getMarketId() { return marketId; }

    public List<OrderBookEntry> getBids() { return bids; }

    public List<OrderBookEntry> getAsks() { return asks; }
}
