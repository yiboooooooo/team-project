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
        List<BookOrder> opposite =
            orderRepo.findOppositeSideOrders(incoming.getMarketId(), incoming.getSide());

        for (BookOrder resting : opposite) {

            if (incomingRemaining <= 0) break;
            if (resting.getRemainingQty() <= 0) continue;
            if (!crosses(incoming, resting)) continue;

            double restingRemaining = resting.getRemainingQty();
            double executedSize = Math.min(incomingRemaining, restingRemaining);

            // --------- Decide who is BUY and who is SELL ---------
            BookOrder buyOrder  = (incoming.getSide() == Side.BUY)  ? incoming : resting;
            BookOrder sellOrder = (incoming.getSide() == Side.SELL) ? incoming : resting;

            // --------- Compute position "price" as quantity ratios ---------
            // Example: incoming qty = 100, resting qty = 20 -> 100/120 vs 20/120
            double buyQty  = buyOrder.getOriginalQty();
            double sellQty = sellOrder.getOriginalQty();
            double totalQty = buyQty + sellQty;
            if (totalQty <= 0) {
                totalQty = 1.0; // safety
            }
            double buyRatio  = buyQty  / totalQty;
            double sellRatio = sellQty / totalQty;

            // Save positions with the ratio stored in the price column
            positionRepo.savePosition(buyOrder,  executedSize, buyRatio);
            positionRepo.savePosition(sellOrder, executedSize, sellRatio);

            // --------- Compute execution price for balances / trades ---------
            Double restPriceObj = resting.getPrice();
            Double inPriceObj   = incoming.getPrice();
            double executedPrice =
                (restPriceObj != null) ? restPriceObj :
                    (inPriceObj   != null) ? inPriceObj   :
                        1.0;   // fallback for true market/market

            // --------- Update remaining_qty in DB ---------
            orderRepo.reduceRemainingQty(incoming.getId(), executedSize);
            orderRepo.reduceRemainingQty(resting.getId(),  executedSize);

            // Keep in-memory orders in sync (so remainingQty is correct if reused)
            incoming.reduce(executedSize);
            resting.reduce(executedSize);
            incomingRemaining -= executedSize;

            // --------- Apply balances + record Trade ---------
            Trade trade = new Trade(
                incoming.getMarketId(),
                buyOrder.getId(),
                sellOrder.getId(),
                executedPrice,
                executedSize
            );
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
     *   BUY:  buyPrice >= sellPrice
     *   SELL: sellPrice <= buyPrice
     */
    private boolean crosses(BookOrder incoming, BookOrder resting) {

        Double inPrice   = incoming.getPrice();
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
