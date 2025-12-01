package stakemate.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import stakemate.data_access.supabase.PostgresOrderRepository;
import stakemate.entity.OrderBook;
import stakemate.entity.OrderBookEntry;
import stakemate.entity.Side;
import stakemate.service.DbAccountService;
import stakemate.use_case.PlaceOrderUseCase.PositionRepository;

/**
 * MatchingEngine holds internal mutable order lists (per market) and performs
 * matching.
 * It produces Trade objects and can render an immutable OrderBook view for UI
 * using OrderBookFactory.
 *
 * Updated to support both In-Memory (legacy) and DB-backed (Postgres) modes.
 */
public class MatchingEngine {

    // --- In-Memory Fields ---
    private final List<BookOrder> bids = new ArrayList<>();
    private final List<BookOrder> asks = new ArrayList<>();

    // --- DB Fields ---
    private PostgresOrderRepository orderRepo;
    private PositionRepository positionRepo;
    private DbAccountService accountService;

    // keep trades for display (shared)
    private final List<Trade> trades = new ArrayList<>();

    /**
     * Default constructor for In-Memory mode.
     */
    public MatchingEngine() {
    }

    /**
     * Constructor for DB-backed mode.
     */
    public MatchingEngine(PostgresOrderRepository orderRepo,
            PositionRepository positionRepo,
            DbAccountService accountService) {
        this.orderRepo = orderRepo;
        this.positionRepo = positionRepo;
        this.accountService = accountService;
    }

    /**
     * Place an order (limit or market). Returns list of trades executed (may be
     * empty).
     */
    public synchronized List<Trade> placeOrder(final BookOrder incoming) {
        if (orderRepo != null) {
            return placeOrderDb(incoming);
        } else {
            return placeOrderInMemory(incoming);
        }
    }

    // --- DB Implementation ---
    private List<Trade> placeOrderDb(BookOrder incoming) {
        List<Trade> executedTrades = new ArrayList<>();
        double incomingRemaining = incoming.getRemainingQty();

        List<BookOrder> opposite = orderRepo.findOppositeSideOrders(incoming.getMarketId(), incoming.getSide());

        for (BookOrder resting : opposite) {

            if (incomingRemaining <= 0)
                break;
            if (resting.getRemainingQty() <= 0)
                continue;
            if (!crosses(incoming, resting))
                continue;

            double restingRemaining = resting.getRemainingQty();
            double executedSize = Math.min(incomingRemaining, restingRemaining);

            BookOrder buyOrder = (incoming.getSide() == Side.BUY) ? incoming : resting;
            BookOrder sellOrder = (incoming.getSide() == Side.SELL) ? incoming : resting;

            // --- FIX 1: Calculate Price FIRST (Moved up) ---
            Double restPriceObj = resting.getPrice();
            Double inPriceObj = incoming.getPrice();
            double executedPrice = (restPriceObj != null) ? restPriceObj : (inPriceObj != null) ? inPriceObj : 1.0;

            // --- FIX 2: Save REAL PRICE to DB (Not Ratio) ---
            positionRepo.savePosition(buyOrder, executedSize, executedPrice);
            positionRepo.savePosition(sellOrder, executedSize, executedPrice);

            // Update remaining_qty
            orderRepo.reduceRemainingQty(incoming.getId(), executedSize);
            orderRepo.reduceRemainingQty(resting.getId(), executedSize);

            incoming.reduce(executedSize);
            resting.reduce(executedSize);
            incomingRemaining -= executedSize;

            Trade trade = new Trade(
                incoming.getMarketId(),
                buyOrder.getId(),
                sellOrder.getId(),
                executedPrice,
                executedSize);

            // --- FIX 3: NO BALANCE CHANGE ---
            // accountService.applyTrade(buyOrder, sellOrder, trade); // Disabled

            executedTrades.add(trade);
            trades.add(trade);

            break;
        }
        return executedTrades;
    }

    private boolean crosses(BookOrder incoming, BookOrder resting) {
        Double inPrice = incoming.getPrice();
        Double restPrice = resting.getPrice();

        // If either is a market order -> always eligible
        if (inPrice == null || restPrice == null) {
            return true;
        }

        if (incoming.getSide() == Side.BUY) {
            return inPrice >= restPrice;
        } else {
            return inPrice <= restPrice;
        }
    }

    // --- In-Memory Implementation ---
    private List<Trade> placeOrderInMemory(final BookOrder incoming) {
        final List<Trade> executed = new ArrayList<>();
        final List<BookOrder> opposite = (incoming.getSide() == Side.BUY) ? asks : bids;
        final Comparator<BookOrder> cmp;
        if (incoming.getSide() == Side.BUY) {
            // match buys against lowest ask price first, then earlier timestamp
            cmp = Comparator.comparing((BookOrder o) -> o.getPrice() == null ? Double.MAX_VALUE : o.getPrice())
                    .thenComparing(BookOrder::getTimestamp);
        } else {
            // match sells against highest bid price first, then earlier timestamp
            cmp = Comparator.comparing((BookOrder o) -> o.getPrice() == null ? Double.MIN_VALUE : o.getPrice())
                    .reversed()
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
            } else {
                if (incoming.getSide() == Side.BUY) {
                    // incoming buy limit must be >= resting sell price
                    crosses = incomingPrice >= restingPrice - 1e-9;
                } else {
                    // incoming sell limit must be <= resting buy price
                    crosses = incomingPrice <= restingPrice + 1e-9;
                }
            }

            if (!crosses) {
                continue;
            }

            // choose trade price: prefer resting order price if available, otherwise
            // incoming price
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

        // after matching: if limit and remainder > 0 => rest in book; if market =>
        // cancel remainder
        if (!incoming.isFilled()) {
            if (incoming.isMarket()) {
                // market remainder cancels -> nothing to add
            } else {
                // rest incoming into book
                if (incoming.getSide() == Side.BUY) {
                    bids.add(incoming);
                } else {
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
        if (orderRepo != null) {
            return snapshotOrderBookDb(marketId);
        } else {
            return snapshotOrderBookInMemory(marketId);
        }
    }

    private OrderBook snapshotOrderBookDb(String marketId) {
        List<BookOrder> dbBids = orderRepo.findOpenOrdersForMarket(marketId, Side.BUY);
        List<BookOrder> dbAsks = orderRepo.findOpenOrdersForMarket(marketId, Side.SELL);

        List<OrderBookEntry> bidEntries = new ArrayList<>();
        List<OrderBookEntry> askEntries = new ArrayList<>();

        for (BookOrder b : dbBids) {
            double priceValue = (b.getPrice() == null) ? -1.0 : b.getPrice();
            bidEntries.add(new OrderBookEntry(Side.BUY, priceValue, b.getRemainingQty()));
        }

        for (BookOrder a : dbAsks) {
            double priceValue = (a.getPrice() == null) ? -1.0 : a.getPrice();
            askEntries.add(new OrderBookEntry(Side.SELL, priceValue, a.getRemainingQty()));
        }

        return new OrderBook(marketId, bidEntries, askEntries);
    }

    private OrderBook snapshotOrderBookInMemory(final String marketId) {
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
