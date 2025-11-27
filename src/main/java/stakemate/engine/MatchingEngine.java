package stakemate.engine;

import stakemate.data_access.supabase.PostgresOrderRepository;
import stakemate.entity.OrderBook;
import stakemate.entity.OrderBookEntry;
import stakemate.use_case.PlaceOrderUseCase.PositionRepository;
import stakemate.service.DbAccountService;
import stakemate.entity.Side;

import java.util.ArrayList;
import java.util.List;

public class MatchingEngine {

    private final PostgresOrderRepository orderRepo;
    private final PositionRepository positionRepo;
    private final DbAccountService accountService;

    public MatchingEngine(PostgresOrderRepository orderRepo,
                          PositionRepository positionRepo,
                          DbAccountService accountService) {
        this.orderRepo = orderRepo;
        this.positionRepo = positionRepo;
        this.accountService = accountService;
    }

    public synchronized List<Trade> placeOrder(BookOrder incoming) {

        List<Trade> trades = new ArrayList<>();

        List<BookOrder> opposite =
            orderRepo.findOppositeSideOrders(incoming.getMarketId(), incoming.getSide());

        double incomingRemaining = incoming.getRemainingQty();

        for (BookOrder resting : opposite) {

            if (incomingRemaining <= 0) break;
            if (resting.getRemainingQty() <= 0) continue;

            if (!crosses(incoming, resting)) continue;

            double executedSize = Math.min(incomingRemaining, resting.getRemainingQty());
            double executedPrice = resting.getPrice();

            // Update DB remaining_qty
            orderRepo.reduceRemainingQty(incoming.getId(), executedSize);
            orderRepo.reduceRemainingQty(resting.getId(), executedSize);

            // Insert BUY + SELL positions
            BookOrder buyOrder = (incoming.getSide() == Side.BUY ? incoming : resting);
            BookOrder sellOrder = (incoming.getSide() == Side.SELL ? incoming : resting);

            positionRepo.savePosition(buyOrder, executedSize, executedPrice);
            positionRepo.savePosition(sellOrder, executedSize, executedPrice);

            // Update balances
            accountService.applyTrade(buyOrder, sellOrder, new Trade(
                incoming.getMarketId(),
                buyOrder.getId(),
                sellOrder.getId(),
                executedPrice,
                executedSize
            ));

            trades.add(new Trade(
                incoming.getMarketId(),
                buyOrder.getId(),
                sellOrder.getId(),
                executedPrice,
                executedSize
            ));

            incomingRemaining -= executedSize;
        }

        return trades;
    }

    private boolean crosses(BookOrder incoming, BookOrder resting) {

        Double inP = incoming.getPrice();
        Double reP = resting.getPrice();

        // If either is a market order -> match
        if (inP == null || reP == null) {
            return true;
        }

        if (incoming.getSide() == Side.BUY) {
            return inP >= reP;
        }
        return inP <= reP;
    }


    public OrderBook snapshotOrderBook(String marketId) {

        List<BookOrder> bids = orderRepo.findOpenOrdersForMarket(marketId, Side.BUY);
        List<BookOrder> asks = orderRepo.findOpenOrdersForMarket(marketId, Side.SELL);

        List<OrderBookEntry> bidEntries = new ArrayList<>();
        List<OrderBookEntry> askEntries = new ArrayList<>();

        for (BookOrder b : bids) {
            if (b.getPrice() != null) {
                bidEntries.add(new OrderBookEntry(Side.BUY, b.getPrice(), b.getRemainingQty()));
            }
        }
        for (BookOrder a : asks) {
            if (a.getPrice() != null) {
                askEntries.add(new OrderBookEntry(Side.SELL, a.getPrice(), a.getRemainingQty()));
            }
        }

        return new OrderBook(marketId, bidEntries, askEntries);
    }



    public List<Trade> getTrades() {
        return List.of(); // UI just needs this method to exist
    }

}
