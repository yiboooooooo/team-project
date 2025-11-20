package stakemate.data_access.adapter;

import stakemate.entity.OrderBook;
import stakemate.entity.OrderBookEntry;
import stakemate.entity.Side;
import stakemate.use_case.view_market.OrderBookGateway;
import stakemate.use_case.view_market.OrderBookSubscriber;
import stakemate.use_case.view_market.RepositoryException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * [Adapter Pattern]
 * Adapts the interface of ExternalLegacyOrderBookService to OrderBookGateway.
 */
public class LegacyOrderBookAdapter implements OrderBookGateway {

    private final ExternalLegacyOrderBookService legacyService;

    public LegacyOrderBookAdapter(ExternalLegacyOrderBookService legacyService) {
        this.legacyService = legacyService;
    }

    @Override
    public OrderBook getSnapshot(String marketId) throws RepositoryException {
        // Translate calls
        Map<String, Double> rawData = legacyService.fetchLegacyData(marketId);

        // Translate Data: Convert Map<"BID_price", qty> to Entity
        List<OrderBookEntry> bids = new ArrayList<>();
        List<OrderBookEntry> asks = new ArrayList<>();

        rawData.forEach((key, qty) -> {
            String[] parts = key.split("_"); // e.g. "BID_0.95"
            if (parts.length == 2) {
                double price = Double.parseDouble(parts[1]);
                if (parts[0].equals("BID")) {
                    bids.add(new OrderBookEntry(Side.BUY, price, qty));
                } else {
                    asks.add(new OrderBookEntry(Side.SELL, price, qty));
                }
            }
        });

        return new OrderBook(marketId, bids, asks);
    }

    @Override
    public void subscribe(String marketId, OrderBookSubscriber subscriber) {
        // Legacy service doesn't support push, so we ignore or implement polling here
        System.out.println("Adapter: Legacy service does not support live sockets.");
    }

    @Override
    public void unsubscribe(String marketId, OrderBookSubscriber subscriber) {
        // No-op
    }
}
