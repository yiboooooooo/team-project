package stakemate.data_access.supabase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import stakemate.engine.BookOrder;
import stakemate.entity.OrderBook;
import stakemate.entity.OrderBookEntry;
import stakemate.entity.Side;
import stakemate.use_case.PlaceOrderUseCase.OrderRepository;
import stakemate.use_case.view_market.OrderBookGateway;
import stakemate.use_case.view_market.OrderBookSubscriber;
import stakemate.use_case.view_market.RepositoryException;

/**
 * [Adapter Pattern]
 * Adapts the PostgresOrderRepository (Data Layer) to the OrderBookGateway interface.
 * Implements thread-safe polling for the Observer Pattern.
 */
public class PostgresOrderBookGateway implements OrderBookGateway {

    private static final long POLLING_INTERVAL_MS = 500;
    private static final double ROUNDING_FACTOR = 100.0;

    private final OrderRepository orderRepository;
    // Use CopyOnWriteArrayList to prevent ConcurrentModificationException during iteration
    private final Map<String, List<OrderBookSubscriber>> subscribers = new ConcurrentHashMap<>();
    private final Timer pollingTimer;

    public PostgresOrderBookGateway(final OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        this.pollingTimer = new Timer("DB_Polling_Timer", true);

        // Poll database every 500ms
        this.pollingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    notifySubscribers();
                }
                // -@cs[IllegalCatch] Catching Exception is necessary to prevent timer thread death
                catch (final Exception ex) {
                    // Catch ALL exceptions to prevent the timer thread from dying
                    System.err.println("Critical error in polling timer: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }, 0, POLLING_INTERVAL_MS);
    }

    @Override
    public OrderBook getSnapshot(final String marketId) throws RepositoryException {
        try {
            final List<BookOrder> rawBids = orderRepository.findOpenOrdersForMarket(marketId, Side.BUY);
            final List<BookOrder> rawAsks = orderRepository.findOpenOrdersForMarket(marketId, Side.SELL);

            final List<OrderBookEntry> bids = aggregateOrders(rawBids, Side.BUY);
            final List<OrderBookEntry> asks = aggregateOrders(rawAsks, Side.SELL);

            return new OrderBook(marketId, bids, asks);
        }
        // -@cs[IllegalCatch] Catching generic Exception to wrap into RepositoryException for callers
        catch (final Exception ex) {
            throw new RepositoryException("Failed to fetch order book: " + ex.getMessage(), ex);
        }
    }

    /**
     * Aggregates orders by price, rounding to 2 decimal places to ensure correct grouping.
     *
     * @param orders The list of raw orders to aggregate.
     * @param side   The side of the book (BUY/SELL) for sorting.
     * @return A list of aggregated OrderBookEntry objects.
     */
    private List<OrderBookEntry> aggregateOrders(final List<BookOrder> orders, final Side side) {
        final List<OrderBookEntry> result;
        if (orders == null || orders.isEmpty()) {
            result = new ArrayList<>();
        }
        else {
            final Map<Double, Double> aggregated = orders.stream()
                .collect(Collectors.groupingBy(
                    this::getRoundedPrice,
                    Collectors.summingDouble(BookOrder::getRemainingQty)
                ));

            final Comparator<OrderBookEntry> comparator;
            if (side == Side.BUY) {
                comparator = Comparator.comparingDouble(OrderBookEntry::getPrice).reversed();
            }
            else {
                comparator = Comparator.comparingDouble(OrderBookEntry::getPrice);
            }

            result = aggregated.entrySet().stream()
                .map(entry -> new OrderBookEntry(side, entry.getKey(), entry.getValue()))
                .sorted(comparator)
                .collect(Collectors.toList());
        }
        return result;
    }

    private double getRoundedPrice(final BookOrder order) {
        final double price;
        if (order.getPrice() == null) {
            price = 0.0;
        }
        else {
            // Round to 2 decimal places to ensure consistent grouping
            price = Math.round(order.getPrice() * ROUNDING_FACTOR) / ROUNDING_FACTOR;
        }
        return price;
    }

    @Override
    public void subscribe(final String marketId, final OrderBookSubscriber subscriber) {
        subscribers.computeIfAbsent(marketId, key -> new CopyOnWriteArrayList<>()).add(subscriber);
        // Send immediate initial data
        try {
            subscriber.onOrderBookUpdated(getSnapshot(marketId));
        }
        catch (final RepositoryException ex) {
            subscriber.onConnectionError("Could not fetch initial data");
        }
    }

    @Override
    public void unsubscribe(final String marketId, final OrderBookSubscriber subscriber) {
        if (subscribers.containsKey(marketId)) {
            subscribers.get(marketId).remove(subscriber);
        }
    }

    private void notifySubscribers() {
        for (final String marketId : subscribers.keySet()) {
            final List<OrderBookSubscriber> list = subscribers.get(marketId);

            // Cleanup empty subscriptions
            if (list == null || list.isEmpty()) {
                subscribers.remove(marketId);
                continue;
            }

            try {
                final OrderBook snapshot = getSnapshot(marketId);
                for (final OrderBookSubscriber sub : list) {
                    sub.onOrderBookUpdated(snapshot);
                }
            }
            catch (final RepositoryException ex) {
                for (final OrderBookSubscriber sub : list) {
                    sub.onConnectionError("Error refreshing: " + ex.getMessage());
                }
            }
        }
    }
}
