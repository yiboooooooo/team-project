package stakemate.engine;

import stakemate.data_access.supabase.PostgresOrderRepository;
import stakemate.entity.OrderBook;
import stakemate.entity.OrderBookEntry;
import stakemate.entity.Side;
import stakemate.service.DbAccountService;
import stakemate.use_case.PlaceOrderUseCase.PositionRepository;

import java.util.ArrayList;
import java.util.List;

public class MatchingEngine {

    private final PostgresOrderRepository orderRepo;
    private final PositionRepository positionRepo;
    private final DbAccountService accountService;

    // keep a simple in-memory trade log for the UI
    private final List<Trade> trades = new ArrayList<>();

    public MatchingEngine(PostgresOrderRepository orderRepo,
            PositionRepository positionRepo,
            DbAccountService accountService) {
        this.orderRepo = orderRepo;
        this.positionRepo = positionRepo;
        this.accountService = accountService;
    }

    public synchronized List<Trade> placeOrder(BookOrder incoming) {

        List<Trade> trades = new ArrayList<>();

        double incomingRemaining = incoming.getRemainingQty();

        // All opposite-side resting orders for this market, best first
        List<BookOrder> opposite = orderRepo.findOppositeSideOrders(incoming.getMarketId(), incoming.getSide());

        for (BookOrder resting : opposite) {

            if (incomingRemaining <= 0)
                break;
            if (resting.getRemainingQty() <= 0)
                continue;
            if (!crosses(incoming, resting))
                continue;

            // --------- Execute BOTH orders fully (Pari-Mutuel style) ---------
            // Since we only create ONE pair per incoming order (break at end),
            // and the user wants the market order to be "completely filled",
            // we treat the match as consuming the ENTIRE available quantity of BOTH orders.

            double executedIncoming = incoming.getRemainingQty();
            double executedResting = resting.getRemainingQty();

            // --------- Decide who is BUY and who is SELL ---------
            BookOrder buyOrder = (incoming.getSide() == Side.BUY) ? incoming : resting;
            BookOrder sellOrder = (incoming.getSide() == Side.SELL) ? incoming : resting;

            // --------- Compute position "price" as quantity ratios ---------
            // Example: incoming qty = 100, resting qty = 20 -> 100/120 vs 20/120
            double buyQty = buyOrder.getOriginalQty();
            double sellQty = sellOrder.getOriginalQty();
            double totalQty = buyQty + sellQty;
            if (totalQty <= 0) {
                totalQty = 1.0; // safety
            }
            double buyRatio = buyQty / totalQty;
            double sellRatio = sellQty / totalQty;

            // Save positions with the ratio stored in the price column
            // Pass the specific executed amount for each side
            double buyExecuted = (incoming.getSide() == Side.BUY) ? executedIncoming : executedResting;
            double sellExecuted = (incoming.getSide() == Side.SELL) ? executedIncoming : executedResting;

            positionRepo.savePosition(buyOrder, buyExecuted, buyRatio);
            positionRepo.savePosition(sellOrder, sellExecuted, sellRatio);

            // --------- Compute execution price for balances / trades ---------
            Double restPriceObj = resting.getPrice();
            Double inPriceObj = incoming.getPrice();
            double executedPrice = (restPriceObj != null) ? restPriceObj : (inPriceObj != null) ? inPriceObj : 1.0; // fallback
                                                                                                                    // for
                                                                                                                    // true
                                                                                                                    // market/market

            // --------- Update remaining_qty in DB (reduce to 0) ---------
            orderRepo.reduceRemainingQty(incoming.getId(), executedIncoming);
            orderRepo.reduceRemainingQty(resting.getId(), executedResting);

            // Keep in-memory orders in sync
            incoming.reduce(executedIncoming);
            resting.reduce(executedResting);
            incomingRemaining = 0; // Fully consumed

            // --------- Apply balances + record Trade ---------
            // Trade size is the total liquidity participating in this match?
            // Or just the incoming amount? Usually trade volume is the matched amount.
            // In this pool logic, maybe it's the sum. Let's use incoming amount for now as
            // it's the "aggressor".
            Trade trade = new Trade(
                    incoming.getMarketId(),
                    buyOrder.getId(),
                    sellOrder.getId(),
                    executedPrice,
                    executedIncoming);
            accountService.applyTrade(buyOrder, sellOrder, trade);
            trades.add(trade);

            // ********** IMPORTANT **********
            // Only a single pair should be created per incoming order,
            // so we stop after the FIRST successful match.
            break;
        }

        return trades;
    }

    /**
     * Price crossing logic:
     * - Any MARKET (price == null) matches any opposite order in the same market.
     * - Limit vs Limit:
     * BUY: buyPrice >= sellPrice
     * SELL: sellPrice <= buyPrice
     */
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

    // === Order book snapshot for the UI (limit orders only) ===
    public OrderBook snapshotOrderBook(String marketId) {

        List<BookOrder> bids = orderRepo.findOpenOrdersForMarket(marketId, Side.BUY);
        List<BookOrder> asks = orderRepo.findOpenOrdersForMarket(marketId, Side.SELL);

        List<OrderBookEntry> bidEntries = new ArrayList<>();
        List<OrderBookEntry> askEntries = new ArrayList<>();

        for (BookOrder b : bids) {
            double priceValue = (b.getPrice() == null) ? -1.0 : b.getPrice();
            bidEntries.add(new OrderBookEntry(Side.BUY, priceValue, b.getRemainingQty()));
        }

        for (BookOrder a : asks) {
            double priceValue = (a.getPrice() == null) ? -1.0 : a.getPrice();
            askEntries.add(new OrderBookEntry(Side.SELL, priceValue, a.getRemainingQty()));
        }

        return new OrderBook(marketId, bidEntries, askEntries);
    }

    // === Trade history for UI ===
    public List<Trade> getTrades() {
        return new ArrayList<>(trades);
    }
}
