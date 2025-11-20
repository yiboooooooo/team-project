package stakemate.engine;

import stakemate.entity.OrderBook;
import stakemate.entity.OrderBookEntry;
import stakemate.entity.Side;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MatchingEngine holds internal mutable order lists (per market) and performs matching.
 * It produces Trade objects and can render an immutable OrderBook view for UI using OrderBookFactory.
 */
public class MatchingEngine {

    // For demo we keep a single market's book in memory; extend to multiple markets by Map<marketId, lists>
    private final List<BookOrder> bids = new ArrayList<>(); // BUY
    private final List<BookOrder> asks = new ArrayList<>(); // SELL

    // keep trades for display
    private final List<Trade> trades = new ArrayList<>();

    public MatchingEngine() { }

    /**
     * Place an order (limit or market). Returns list of trades executed (may be empty).
     * Remainder: limit rests in the book; market remainder is cancelled.
     */
    public synchronized List<Trade> placeOrder(BookOrder incoming) {
        List<Trade> executed = new ArrayList<>();

        // choose opposite list
        List<BookOrder> opposite = (incoming.getSide() == Side.BUY) ? asks : bids;

        // sort opposite by price-time priority:
        Comparator<BookOrder> cmp;
        if (incoming.getSide() == Side.BUY) {
            // match buys against lowest ask price first, then earlier timestamp
            cmp = Comparator.comparing((BookOrder o) -> o.getPrice() == null ? Double.MAX_VALUE : o.getPrice())
                    .thenComparing(BookOrder::getTimestamp);
        } else {
            // match sells against highest bid price first, then earlier timestamp
            cmp = Comparator.comparing((BookOrder o) -> o.getPrice() == null ? Double.MIN_VALUE : o.getPrice()).reversed()
                    .thenComparing(BookOrder::getTimestamp);
        }
        List<BookOrder> sortedOpposite = opposite.stream().sorted(cmp).collect(Collectors.toList());

        Iterator<BookOrder> it = sortedOpposite.iterator();
        while (!incoming.isFilled() && it.hasNext()) {
            BookOrder resting = it.next();

            // Price crossing rules:
            // - If incoming is market -> always eligible
            // - If incoming is limit -> must cross: buy.price >= sell.price
            Double restingPrice = resting.getPrice();
            Double incomingPrice = incoming.getPrice();

            boolean crosses;
            if (incoming.isMarket() || restingPrice == null) {
                crosses = true;
            } else {
                if (incoming.getSide() == Side.BUY) {
                    // incoming buy limit must be >= resting sell price
                    crosses = incomingPrice >= restingPrice - 1e-9;
                } else {
                    // incoming sell limit must be <= resting buy price
                    crosses = incomingPrice <= restingPrice + 1e-9;
                }
            }

            if (!crosses) continue;

            // choose trade price: prefer resting order price if available, otherwise incoming price
            Double tradePrice = (restingPrice != null) ? restingPrice : incomingPrice;
            if (tradePrice == null) {
                // both market — choose 1.0 fallback
                tradePrice = 1.0;
            }

            double tradeSize = Math.min(incoming.getRemainingQty(), resting.getRemainingQty());
            if (tradeSize <= 0) continue;

            // build trade (buyOrderId first)
            String buyId = (incoming.getSide() == Side.BUY) ? incoming.getId() : resting.getId();
            String sellId = (incoming.getSide() == Side.SELL) ? incoming.getId() : resting.getId();

            Trade t = new Trade(incoming.getMarketId(), buyId, sellId, tradePrice, tradeSize);
            executed.add(t);
            trades.add(t);

            // apply fills
            incoming.reduce(tradeSize);
            // find the original resting in the real opposite list and reduce it
            reduceResting(resting.getId(), tradeSize);

            // if resting is exhausted remove from real list
            removeFilledFromLists();
        }

        // after matching: if limit and remainder > 0 => rest in book; if market => cancel remainder
        if (!incoming.isFilled()) {
            if (incoming.isMarket()) {
                // market remainder cancels -> nothing to add
            } else {
                // rest incoming into book
                if (incoming.getSide() == Side.BUY) bids.add(incoming);
                else asks.add(incoming);
            }
        }
        return executed;
    }

    // helper to reduce the actual resting order in real lists (not the sorted copy)
    private void reduceResting(String restingId, double qty) {
        for (BookOrder o : bids) {
            if (o.getId().equals(restingId)) { o.reduce(qty); return; }
        }
        for (BookOrder o : asks) {
            if (o.getId().equals(restingId)) { o.reduce(qty); return; }
        }
    }

    private void removeFilledFromLists() {
        bids.removeIf(BookOrder::isFilled);
        asks.removeIf(BookOrder::isFilled);
    }

    public synchronized OrderBook snapshotOrderBook(String marketId) {
        // aggregate price levels into OrderBookEntry lists
        Map<Double, Double> bidAgg = new TreeMap<>(Comparator.reverseOrder()); // highest first
        Map<Double, Double> askAgg = new TreeMap<>(); // lowest first

        for (BookOrder b : bids) {
            Double p = b.getPrice() == null ? 0.0 : b.getPrice();
            bidAgg.put(p, bidAgg.getOrDefault(p, 0.0) + b.getRemainingQty());
        }
        for (BookOrder a : asks) {
            Double p = a.getPrice() == null ? 0.0 : a.getPrice();
            askAgg.put(p, askAgg.getOrDefault(p, 0.0) + a.getRemainingQty());
        }

        List<OrderBookEntry> bidEntries = bidAgg.entrySet().stream()
                .map(e -> new OrderBookEntry(stakemate.entity.Side.BUY, e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        List<OrderBookEntry> askEntries = askAgg.entrySet().stream()
                .map(e -> new OrderBookEntry(stakemate.entity.Side.SELL, e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return new OrderBook(marketId, bidEntries, askEntries);
    }

    public List<Trade> getTrades() { return Collections.unmodifiableList(trades); }

    // convenience getters for demo/UI
    public List<BookOrder> getBids() { return Collections.unmodifiableList(bids); }
    public List<BookOrder> getAsks() { return Collections.unmodifiableList(asks); }
}
