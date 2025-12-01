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

        // All opposite-side resting orders for this market, best first
        List<BookOrder> opposite = orderRepo.findOppositeSideOrders(incoming.getMarketId(), incoming.getSide());

        // --- Market Order Pre-Check (All-Or-None) ---
        if (incoming.isMarket()) {
            double simulatedFilled = 0;
            double simulatedCost = 0;
            double tempRemaining = incoming.getOriginalQty();

            for (BookOrder resting : opposite) {
                if (tempRemaining <= 0)
                    break;
                if (resting.getRemainingQty() <= 0)
                    continue;
                if (resting.isMarket())
                    continue; // Market orders only match Limit orders

                double matchSize = Math.min(tempRemaining, resting.getRemainingQty());
                Double p = resting.getPrice();
                if (p == null)
                    continue; // Should be covered by isMarket(), but safety first
                double matchPrice = p;

                simulatedFilled += matchSize;
                simulatedCost += matchSize * matchPrice;
                tempRemaining -= matchSize;
            }

            // Check 1: Full Fill Required - REMOVED to allow resting market orders
            // if (tempRemaining > 1e-9) {
            // // Cancel order (insufficient liquidity)
            // orderRepo.updateRemainingQty(incoming.getId(), 0.0);
            // incoming.reduce(incoming.getRemainingQty());
            // return executedTrades;
            // }

            // Check 2: Sufficient Funds for IMMEDIATE matches
            // We only check funds for what can be matched RIGHT NOW.
            // If simulatedCost > balance, we cancel.
            // Note: If simulatedCost is 0 (no matches), we allow it to rest (cost is
            // unknown).
            double balance = accountService.getBalance(incoming.getUserId());
            if (simulatedCost > 0 && balance < simulatedCost) {
                // Cancel order (insufficient funds for immediate match)
                orderRepo.updateRemainingQty(incoming.getId(), 0.0);
                incoming.reduce(incoming.getRemainingQty());
                return executedTrades;
            }
        }

        for (BookOrder resting : opposite) {

            if (incomingRemaining <= 0)
                break;
            if (resting.getRemainingQty() <= 0)
                continue;

            // Market orders only match Limit orders
            if (incoming.isMarket() && resting.isMarket())
                continue;

            if (!crosses(incoming, resting))
                continue;

            // --- Calculate execution price for funds check ---
            Double restPriceObj = resting.getPrice();
            Double inPriceObj = incoming.getPrice();
            double executionPrice = (restPriceObj != null) ? restPriceObj : (inPriceObj != null) ? inPriceObj : 1.0;
            double potentialMatchSize = Math.min(incomingRemaining, resting.getRemainingQty());
            double matchCost = potentialMatchSize * executionPrice;

            // --- Dynamic Funds Check for Incoming Order (BUY side only) ---
            if (incoming.getSide() == Side.BUY) {
                double incomingBalance = accountService.getBalance(incoming.getUserId());
                if (incomingBalance < matchCost) {
                    // Insufficient funds for this match -> Cancel incoming order
                    orderRepo.updateRemainingQty(incoming.getId(), 0.0);
                    incoming.reduce(incoming.getRemainingQty());
                    break; // Exit matching loop
                }
            }

            // --- Dynamic Funds Check for Resting Order (BUY side only) ---
            if (resting.getSide() == Side.BUY) {
                double restingBalance = accountService.getBalance(resting.getUserId());
                if (restingBalance < matchCost) {
                    // Insufficient funds for this match -> Cancel resting order
                    orderRepo.updateRemainingQty(resting.getId(), 0.0);
                    resting.reduce(resting.getRemainingQty());
                    continue; // Skip this resting order
                }
            }
            // ---------------------------------------------------

            double restingRemaining = resting.getRemainingQty();
            double executedSize = Math.min(incomingRemaining, restingRemaining);

            // --------- Decide who is BUY and who is SELL ---------
            BookOrder buyOrder = (incoming.getSide() == Side.BUY) ? incoming : resting;
            BookOrder sellOrder = (incoming.getSide() == Side.SELL) ? incoming : resting;

            // --------- Compute position "price" as quantity ratios ---------
            double buyQty = buyOrder.getOriginalQty();
            double sellQty = sellOrder.getOriginalQty();
            double totalQty = buyQty + sellQty;
            if (totalQty <= 0) {
                totalQty = 1.0; // safety
            }
            double buyRatio = buyQty / totalQty;
            double sellRatio = sellQty / totalQty;

            // Save positions with the ratio stored in the price column
            positionRepo.savePosition(buyOrder, executedSize, buyRatio);
            positionRepo.savePosition(sellOrder, executedSize, sellRatio);

            // --------- Update remaining_qty in DB ---------
            orderRepo.reduceRemainingQty(incoming.getId(), executedSize);
            orderRepo.reduceRemainingQty(resting.getId(), executedSize);

            // Keep in-memory orders in sync (so remainingQty is correct if reused)
            incoming.reduce(executedSize);
            resting.reduce(executedSize);
            incomingRemaining -= executedSize;

            // --------- Apply balances + record Trade ---------
            Trade trade = new Trade(
                    incoming.getMarketId(),
                    buyOrder.getId(),
                    sellOrder.getId(),
                    executionPrice,
                    executedSize);
            accountService.applyTrade(buyOrder, sellOrder, trade);
            executedTrades.add(trade);
            trades.add(trade); // Keep in-memory log

            // Removed break to allow partial fills / multiple matches
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
