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

    private static final long TIMER_PERIOD_MS = 2000L;
    private static final double SIMULATED_ERROR_RATE = 0.05;
    private static final double RECONNECT_RATE = 0.1;
    private static final double EMPTY_BOOK_PROBABILITY = 0.3;
    private static final int BOOK_DEPTH = 3;
    private static final double BASE_PRICE = 2.0;
    private static final double PRICE_INCREMENT = 0.1;
    private static final int BASE_QTY = 10;
    private static final int QTY_VARIANCE = 20;

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
        }, TIMER_PERIOD_MS, TIMER_PERIOD_MS);
    }

    @Override
    public OrderBook getSnapshot(final String marketId) throws RepositoryException {
        if (random.nextDouble() < SIMULATED_ERROR_RATE) {
            throw new RepositoryException("Simulated DB connectivity issue.");
        }
        return orderBooks.computeIfAbsent(marketId, this::createRandomOrderBookPossiblyEmpty);
    }

    @Override
    public void subscribe(final String marketId, final OrderBookSubscriber subscriber) {
        subscribers.computeIfAbsent(marketId, key -> new ArrayList<>()).add(subscriber);
    }

    @Override
    public void unsubscribe(final String marketId, final OrderBookSubscriber subscriber) {
        final List<OrderBookSubscriber> list = subscribers.get(marketId);
        if (list != null) {
            list.remove(subscriber);
        }
    }

    private OrderBook createRandomOrderBookPossiblyEmpty(final String marketId) {
        final boolean startEmpty = random.nextDouble() < EMPTY_BOOK_PROBABILITY;
        if (startEmpty) {
            return new OrderBook(marketId, Collections.emptyList(), Collections.emptyList());
        }
        return createPopulatedOrderBook(marketId);
    }

    private OrderBook createPopulatedOrderBook(final String marketId) {
        final List<OrderBookEntry> bids = new ArrayList<>();
        final List<OrderBookEntry> asks = new ArrayList<>();

        final double mid = BASE_PRICE + random.nextDouble();

        for (int i = 0; i < BOOK_DEPTH; i++) {
            final double bidPrice = mid - PRICE_INCREMENT * i;
            final double askPrice = mid + PRICE_INCREMENT * i;
            final double bidQty = BASE_QTY + random.nextInt(QTY_VARIANCE);
            final double askQty = BASE_QTY + random.nextInt(QTY_VARIANCE);

            bids.add(new OrderBookEntry(Side.BUY, bidPrice, bidQty));
            asks.add(new OrderBookEntry(Side.SELL, askPrice, askQty));
        }

        return new OrderBook(marketId, bids, asks);
    }

    private void updateAllOrderBooks() {
        for (final String marketId : new ArrayList<>(orderBooks.keySet())) {
            if (random.nextDouble() < SIMULATED_ERROR_RATE) {
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
                    if (random.nextDouble() < RECONNECT_RATE) {
                        s.onConnectionRestored();
                    }
                }
            }
        }
    }
}
