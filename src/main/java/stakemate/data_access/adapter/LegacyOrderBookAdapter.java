package stakemate.data_access.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import stakemate.entity.OrderBook;
import stakemate.entity.OrderBookEntry;
import stakemate.entity.Side;
import stakemate.use_case.view_market.OrderBookGateway;
import stakemate.use_case.view_market.OrderBookSubscriber;
import stakemate.use_case.view_market.RepositoryException;

/**
 * [Adapter Pattern]
 * Adapts the interface of ExternalLegacyOrderBookService to OrderBookGateway.
 */
public class LegacyOrderBookAdapter implements OrderBookGateway {

    private final ExternalLegacyOrderBookService legacyService;

    public LegacyOrderBookAdapter(final ExternalLegacyOrderBookService legacyService) {
        this.legacyService = legacyService;
    }

    @Override
    public OrderBook getSnapshot(final String marketId) throws RepositoryException {
        // Translate calls
        final Map<String, Double> rawData = legacyService.fetchLegacyData(marketId);
        final List<OrderBookEntry> bids = new ArrayList<>();
        final List<OrderBookEntry> asks = new ArrayList<>();

        rawData.forEach((key, qty) -> {
            final String[] parts = key.split("_");
            if (parts.length == 2) {
                final double price = Double.parseDouble(parts[1]);
                if (parts[0].equals("BID")) {
                    bids.add(new OrderBookEntry(Side.BUY, price, qty));
                }
                else {
                    asks.add(new OrderBookEntry(Side.SELL, price, qty));
                }
            }
        });

        return new OrderBook(marketId, bids, asks);
    }

    @Override
    public void subscribe(final String marketId, final OrderBookSubscriber subscriber) {
        // Legacy service doesn't support push, so we ignore or implement polling here
        System.out.println("Adapter: Legacy service does not support live sockets.");
    }

    @Override
    public void unsubscribe(final String marketId, final OrderBookSubscriber subscriber) {
        // No-op
    }
}
