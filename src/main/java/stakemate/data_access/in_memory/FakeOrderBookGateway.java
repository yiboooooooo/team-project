package stakemate.data_access.in_memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import stakemate.entity.OrderBook;
import stakemate.entity.OrderBookEntry;
import stakemate.entity.Side;
import stakemate.use_case.view_market.OrderBookGateway;
import stakemate.use_case.view_market.OrderBookSubscriber;
import stakemate.use_case.view_market.RepositoryException;

public class FakeOrderBookGateway implements OrderBookGateway {

    private final Map<String, OrderBook> orderBooks = new ConcurrentHashMap<>();
    private final Map<String, List<OrderBookSubscriber>> subscribers = new ConcurrentHashMap<>();
    private final Timer timer = new Timer("OrderBookTimer", true);
    private final Random random = new Random();

    public FakeOrderBookGateway() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateAllOrderBooks();
            }
        }, 2000L, 2000L);
    }

    @Override
    public OrderBook getSnapshot(final String marketId) throws RepositoryException {
        if (random.nextDouble() < 0.05) {
            throw new RepositoryException("Simulated DB connectivity issue.");
        }
        return orderBooks.computeIfAbsent(marketId, this::createRandomOrderBookPossiblyEmpty);
    }

    @Override
    public void subscribe(final String marketId, final OrderBookSubscriber subscriber) {
        subscribers.computeIfAbsent(marketId, k -> new ArrayList<>()).add(subscriber);
    }

    @Override
    public void unsubscribe(final String marketId, final OrderBookSubscriber subscriber) {
        final List<OrderBookSubscriber> list = subscribers.get(marketId);
        if (list != null) {
            list.remove(subscriber);
        }
    }

    private OrderBook createRandomOrderBookPossiblyEmpty(final String marketId) {
        final boolean startEmpty = random.nextDouble() < 0.3;
        if (startEmpty) {
            return new OrderBook(marketId, Collections.emptyList(), Collections.emptyList());
        }
        return createPopulatedOrderBook(marketId);
    }

    private OrderBook createPopulatedOrderBook(final String marketId) {
        final List<OrderBookEntry> bids = new ArrayList<>();
        final List<OrderBookEntry> asks = new ArrayList<>();

        final double mid = 2.0 + random.nextDouble();

        for (int i = 0; i < 3; i++) {
            final double bidPrice = mid - 0.1 * i;
            final double askPrice = mid + 0.1 * i;
            final double bidQty = 10 + random.nextInt(20);
            final double askQty = 10 + random.nextInt(20);

            bids.add(new OrderBookEntry(Side.BUY, bidPrice, bidQty));
            asks.add(new OrderBookEntry(Side.SELL, askPrice, askQty));
        }

        return new OrderBook(marketId, bids, asks);
    }

    private void updateAllOrderBooks() {
        for (final String marketId : new ArrayList<>(orderBooks.keySet())) {
            if (random.nextDouble() < 0.05) {
                final List<OrderBookSubscriber> subs = subscribers.get(marketId);
                if (subs != null) {
                    for (final OrderBookSubscriber s : subs) {
                        s.onConnectionError("Reconnecting...");
                    }
                }
                continue;
            }

            final OrderBook updated = createPopulatedOrderBook(marketId);
            orderBooks.put(marketId, updated);

            final List<OrderBookSubscriber> subs = subscribers.get(marketId);
            if (subs != null) {
                for (final OrderBookSubscriber s : subs) {
                    s.onOrderBookUpdated(updated);
                    if (random.nextDouble() < 0.1) {
                        s.onConnectionRestored();
                    }
                }
            }
        }
    }
}
