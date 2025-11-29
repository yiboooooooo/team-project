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
        boolean isMarketOrder = (incoming.getPrice() == null);

        // All opposite-side resting orders for this market, best first
        List<BookOrder> opposite = orderRepo.findOppositeSideOrders(incoming.getMarketId(), incoming.getSide());

        for (BookOrder resting : opposite) {

            if (incomingRemaining <= 0)
                break;
            if (resting.getRemainingQty() <= 0)
                continue;
            if (!crosses(incoming, resting))
                continue;

            // --------- Determine execution amount ---------

            if (isMarketOrder) {
                // Market orders: FULLY consume BOTH orders (pari-mutuel style)
                // The market order qty and resting limit order qty both get executed fully
                double marketOrderQty = incoming.getRemainingQty();
                double limitOrderQty = resting.getRemainingQty();

                // Execute the trade with BOTH quantities
                // (returns false if cancelled due to insufficient funds)
                boolean success = executeTrade(incoming, resting, marketOrderQty, limitOrderQty, trades);

                if (!success) {
                    // Market order was cancelled due to insufficient funds
                    // Exit without creating any trades
                    break;
                }

                incomingRemaining = 0; // Market order fully consumed

                // Market orders only match once (with first available limit order)
                break;
            } else {
                // Limit orders: allow partial fills
                double executedSize = Math.min(incomingRemaining, resting.getRemainingQty());

                // Execute the trade
                boolean success = executeTrade(incoming, resting, executedSize, executedSize, trades);

                if (!success) {
                    // This shouldn't happen for limit orders (balance already deducted)
                    // but handle gracefully
                    break;
                }

                incomingRemaining -= executedSize;

                // Continue matching if there's still quantity remaining
                if (incomingRemaining <= 0) {
                    break;
                }
            }
        }

        return trades;
    }

    /**
     * Execute a trade between two orders
     * Returns true if trade executed successfully, false if cancelled due to
     * insufficient funds
     * 
     * @param incomingExecutedQty - quantity to execute from incoming order
     * @param restingExecutedQty  - quantity to execute from resting order
     */
    private boolean executeTrade(BookOrder incoming, BookOrder resting,
            double incomingExecutedQty, double restingExecutedQty,
            List<Trade> trades) {
        boolean isMarketOrder = (incoming.getPrice() == null);

        // For market orders, check balance using limit order price BEFORE executing
        if (isMarketOrder) {
            Double limitPrice = resting.getPrice();
            if (limitPrice == null) {
                // This shouldn't happen (market orders should only match with limit orders)
                // but handle gracefully
                System.err.println("WARNING: Market order matched with another market order");
                return false;
            }

            // Use the market order quantity for balance calculation
            double requiredBalance = limitPrice * incomingExecutedQty;

            // Check if user has sufficient funds
            if (accountService instanceof stakemate.service.DbAccountService) {
                stakemate.service.DbAccountService dbAcc = (stakemate.service.DbAccountService) accountService;

                // Try to get current balance - this is a simplified check
                // In a real system, you'd query the actual balance from DB
                // For now, we'll attempt to deduct and handle failure
                try {
                    dbAcc.adjustBalance(incoming.getUserId(), -requiredBalance);
                } catch (Exception e) {
                    // Insufficient funds - cancel the market order
                    System.out.println("Market order cancelled: insufficient funds for user " + incoming.getUserId());

                    // Mark the market order as cancelled by setting remaining to 0
                    // but don't create a position or trade
                    orderRepo.reduceRemainingQty(incoming.getId(), incoming.getRemainingQty());
                    incoming.reduce(incoming.getRemainingQty());

                    return false;
                }
            }
        }

        // --------- Decide who is BUY and who is SELL ---------
        BookOrder buyOrder = (incoming.getSide() == Side.BUY) ? incoming : resting;
        BookOrder sellOrder = (incoming.getSide() == Side.SELL) ? incoming : resting;

        // --------- Compute position "price" as quantity ratios ---------
        BookOrder originalBuyOrder = (incoming.getSide() == Side.BUY) ? incoming : resting;
        BookOrder originalSellOrder = (incoming.getSide() == Side.SELL) ? incoming : resting;

        double buyQty = originalBuyOrder.getOriginalQty();
        double sellQty = originalSellOrder.getOriginalQty();
        double totalQty = buyQty + sellQty;
        if (totalQty <= 0) {
            totalQty = 1.0; // safety
        }
        double buyRatio = buyQty / totalQty;
        double sellRatio = sellQty / totalQty;

        // Determine executed quantities for buy and sell sides
        double buyExecutedQty = (incoming.getSide() == Side.BUY) ? incomingExecutedQty : restingExecutedQty;
        double sellExecutedQty = (incoming.getSide() == Side.SELL) ? incomingExecutedQty : restingExecutedQty;

        // Save positions with the ratio stored in the price column
        positionRepo.savePosition(buyOrder, buyExecutedQty, buyRatio);
        positionRepo.savePosition(sellOrder, sellExecutedQty, sellRatio);

        // --------- Compute execution price for balances / trades ---------
        Double restPriceObj = resting.getPrice();
        Double inPriceObj = incoming.getPrice();
        // Use the limit order's price (resting order should always have a price since
        // market orders match with limits)
        double executedPrice = (restPriceObj != null) ? restPriceObj : (inPriceObj != null) ? inPriceObj : 1.0;

        // --------- Update remaining_qty in DB ---------
        orderRepo.reduceRemainingQty(incoming.getId(), incomingExecutedQty);
        orderRepo.reduceRemainingQty(resting.getId(), restingExecutedQty);

        // Keep in-memory orders in sync
        incoming.reduce(incomingExecutedQty);
        resting.reduce(restingExecutedQty);

        // --------- Apply balances + record Trade ---------
        // For trade volume, use the incoming order quantity (the aggressor)
        Trade trade = new Trade(
                incoming.getMarketId(),
                buyOrder.getId(),
                sellOrder.getId(),
                executedPrice,
                incomingExecutedQty);
        accountService.applyTrade(buyOrder, sellOrder, trade);
        trades.add(trade);

        return true;
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
