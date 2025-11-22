package stakemate.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import stakemate.entity.OrderBook;
import stakemate.entity.OrderBookEntry;
import stakemate.entity.Side;

/**
 * MatchingEngine holds internal mutable order lists (per market) and performs matching.
 * It produces Trade objects and can render an immutable OrderBook view for UI using OrderBookFactory.
 */
public class MatchingEngine {

    // For demo we keep a single market's book in memory; extend to multiple markets by Map<marketId, lists>
    private final List<BookOrder> bids = new ArrayList<>();
    private final List<BookOrder> asks = new ArrayList<>();

    // keep trades for display
    private final List<Trade> trades = new ArrayList<>();

    public MatchingEngine() {
    }

    /**
     * Place an order (limit or market). Returns list of trades executed (may be empty).
     * Remainder: limit rests in the book; market remainder is cancelled.
     */
    public synchronized List<Trade> placeOrder(final BookOrder incoming) {
        final List<Trade> executed = new ArrayList<>();
        final List<BookOrder> opposite = (incoming.getSide() == Side.BUY) ? asks : bids;
        final Comparator<BookOrder> cmp;
        if (incoming.getSide() == Side.BUY) {
            // match buys against lowest ask price first, then earlier timestamp
            cmp = Comparator.comparing((BookOrder o) -> o.getPrice() == null ? Double.MAX_VALUE : o.getPrice())
                .thenComparing(BookOrder::getTimestamp);
        }
        else {
            // match sells against highest bid price first, then earlier timestamp
            cmp = Comparator.comparing((BookOrder o) -> o.getPrice() == null ? Double.MIN_VALUE : o.getPrice()).reversed()
                .thenComparing(BookOrder::getTimestamp);
        }
        final List<BookOrder> sortedOpposite = opposite.stream().sorted(cmp).collect(Collectors.toList());

        final Iterator<BookOrder> it = sortedOpposite.iterator();
        while (!incoming.isFilled() && it.hasNext()) {
            final BookOrder resting = it.next();
            // - If incoming is market -> always eligible
            // - If incoming is limit -> must cross: buy.price >= sell.price
            final Double restingPrice = resting.getPrice();
            final Double incomingPrice = incoming.getPrice();

            final boolean crosses;
            if (incoming.isMarket() || restingPrice == null) {
                crosses = true;
            }
            else {
                if (incoming.getSide() == Side.BUY) {
                    // incoming buy limit must be >= resting sell price
                    crosses = incomingPrice >= restingPrice - 1e-9;
                }
                else {
                    // incoming sell limit must be <= resting buy price
                    crosses = incomingPrice <= restingPrice + 1e-9;
                }
            }

            if (!crosses) {
                continue;
            }

            // choose trade price: prefer resting order price if available, otherwise incoming price
            Double tradePrice = (restingPrice != null) ? restingPrice : incomingPrice;
            if (tradePrice == null) {
                // both market — choose 1.0 fallback
                tradePrice = 1.0;
            }

            final double tradeSize = Math.min(incoming.getRemainingQty(), resting.getRemainingQty());
            if (tradeSize <= 0) {
                continue;
            }

            // build trade (buyOrderId first)
            final String buyId = (incoming.getSide() == Side.BUY) ? incoming.getId() : resting.getId();
            final String sellId = (incoming.getSide() == Side.SELL) ? incoming.getId() : resting.getId();

            final Trade t = new Trade(incoming.getMarketId(), buyId, sellId, tradePrice, tradeSize);
            executed.add(t);
            trades.add(t);
            incoming.reduce(tradeSize);
            reduceResting(resting.getId(), tradeSize);
            removeFilledFromLists();
        }

        // after matching: if limit and remainder > 0 => rest in book; if market => cancel remainder
        if (!incoming.isFilled()) {
            if (incoming.isMarket()) {
                // market remainder cancels -> nothing to add
            }
            else {
                // rest incoming into book
                if (incoming.getSide() == Side.BUY) {
                    bids.add(incoming);
                }
                else {
                    asks.add(incoming);
                }
            }
        }
        return executed;
    }

    // helper to reduce the actual resting order in real lists (not the sorted copy)
    private void reduceResting(final String restingId, final double qty) {
        for (final BookOrder o : bids) {
            if (o.getId().equals(restingId)) {
                o.reduce(qty);
                return;
            }
        }
        for (final BookOrder o : asks) {
            if (o.getId().equals(restingId)) {
                o.reduce(qty);
                return;
            }
        }
    }

    private void removeFilledFromLists() {
        bids.removeIf(BookOrder::isFilled);
        asks.removeIf(BookOrder::isFilled);
    }

    public synchronized OrderBook snapshotOrderBook(final String marketId) {
        // aggregate price levels into OrderBookEntry lists
        final Map<Double, Double> bidAgg = new TreeMap<>(Comparator.reverseOrder());
        final Map<Double, Double> askAgg = new TreeMap<>();

        for (final BookOrder b : bids) {
            final Double p = b.getPrice() == null ? 0.0 : b.getPrice();
            bidAgg.put(p, bidAgg.getOrDefault(p, 0.0) + b.getRemainingQty());
        }
        for (final BookOrder a : asks) {
            final Double p = a.getPrice() == null ? 0.0 : a.getPrice();
            askAgg.put(p, askAgg.getOrDefault(p, 0.0) + a.getRemainingQty());
        }

        final List<OrderBookEntry> bidEntries = bidAgg.entrySet().stream()
            .map(e -> new OrderBookEntry(stakemate.entity.Side.BUY, e.getKey(), e.getValue()))
            .collect(Collectors.toList());

        final List<OrderBookEntry> askEntries = askAgg.entrySet().stream()
            .map(e -> new OrderBookEntry(stakemate.entity.Side.SELL, e.getKey(), e.getValue()))
            .collect(Collectors.toList());

        return new OrderBook(marketId, bidEntries, askEntries);
    }

    public List<Trade> getTrades() {
        return Collections.unmodifiableList(trades);
    }

    // convenience getters for demo/UI
    public List<BookOrder> getBids() {
        return Collections.unmodifiableList(bids);
    }

    public List<BookOrder> getAsks() {
        return Collections.unmodifiableList(asks);
    }
}
