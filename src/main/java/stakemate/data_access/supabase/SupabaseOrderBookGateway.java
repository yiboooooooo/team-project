package stakemate.data_access.supabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import stakemate.entity.OrderBook;
import stakemate.entity.OrderBookEntry;
import stakemate.entity.Side;
import stakemate.use_case.view_market.OrderBookGateway;
import stakemate.use_case.view_market.OrderBookSubscriber;
import stakemate.use_case.view_market.RepositoryException;

public class SupabaseOrderBookGateway implements OrderBookGateway {

    private static final long POLLING_INTERVAL = 3000L;

    private final SupabaseClientFactory factory;
    private final Map<String, List<OrderBookSubscriber>> subscribers = new ConcurrentHashMap<>();
    private final Timer pollingTimer;

    public SupabaseOrderBookGateway(final SupabaseClientFactory factory) {
        this.factory = factory;
        this.pollingTimer = new Timer("SupabaseOrderBookPoller", true);
        this.pollingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                notifySubscribers();
            }
        }, POLLING_INTERVAL, POLLING_INTERVAL);
    }

    @Override
    public OrderBook getSnapshot(final String marketId) throws RepositoryException {
        // DEBUG PRINT to help confirm IDs match
        System.out.println("DEBUG: Fetching orders for Market ID: " + marketId);

        // FIX: Using 'remaining_qty' as per your DBeaver screenshot
        final String sql = "SELECT side, price, remaining_qty FROM public.orders WHERE market_id = ?";

        try (Connection conn = factory.createConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, marketId);

            try (ResultSet rs = ps.executeQuery()) {
                return createOrderBookFromResultSet(marketId, rs);
            }
        }
        catch (final SQLException ex) {
            System.err.println("SQL ERROR: " + ex.getMessage());
            throw new RepositoryException("Failed to fetch order book", ex);
        }
    }

    @Override
    public void subscribe(final String marketId, final OrderBookSubscriber subscriber) {
        subscribers.computeIfAbsent(marketId, key -> new ArrayList<>()).add(subscriber);
    }

    @Override
    public void unsubscribe(final String marketId, final OrderBookSubscriber subscriber) {
        if (subscribers.containsKey(marketId)) {
            subscribers.get(marketId).remove(subscriber);
        }
    }

    private OrderBook createOrderBookFromResultSet(final String marketId,
                                                   final ResultSet resultSet) throws SQLException {
        final Map<Double, Double> buyAgg = new HashMap<>();
        final Map<Double, Double> sellAgg = new HashMap<>();
        int rowCount = 0;

        while (resultSet.next()) {
            rowCount++;
            final String sideStr = resultSet.getString("side");
            final double price = resultSet.getDouble("price");
            final double qty = resultSet.getDouble("remaining_qty");

            if ("BUY".equalsIgnoreCase(sideStr)) {
                buyAgg.merge(price, qty, Double::sum);
            }
            else if ("SELL".equalsIgnoreCase(sideStr)) {
                sellAgg.merge(price, qty, Double::sum);
            }
        }

        if (rowCount > 0) {
            System.out.println("DEBUG: Found " + rowCount + " orders in DB for this market.");
        }

        final List<OrderBookEntry> bids = new ArrayList<>();
        buyAgg.forEach((price, quantity) -> bids.add(new OrderBookEntry(Side.BUY, price, quantity)));

        final List<OrderBookEntry> asks = new ArrayList<>();
        sellAgg.forEach((price, quantity) -> asks.add(new OrderBookEntry(Side.SELL, price, quantity)));

        return new OrderBook(marketId, bids, asks);
    }

    private void notifySubscribers() {
        for (String marketId : subscribers.keySet()) {
            final List<OrderBookSubscriber> marketSubs = subscribers.get(marketId);
            if (marketSubs == null || marketSubs.isEmpty()) {
                continue;
            }

            try {
                final OrderBook snapshot = getSnapshot(marketId);
                for (OrderBookSubscriber sub : marketSubs) {
                    sub.onOrderBookUpdated(snapshot);
                    sub.onConnectionRestored();
                }
            }
            catch (final RepositoryException ex) {
                for (OrderBookSubscriber sub : marketSubs) {
                    sub.onConnectionError("Sync Error");
                }
            }
        }
    }
}
