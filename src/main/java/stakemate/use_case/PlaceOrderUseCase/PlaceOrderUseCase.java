package stakemate.use_case.PlaceOrderUseCase;


import java.util.List;

import stakemate.data_access.supabase.PostgresOrderRepository;
import stakemate.engine.BookOrder;
import stakemate.engine.MatchingEngine;
import stakemate.engine.Trade;
import stakemate.service.AccountService;
import java.util.ArrayList;
import java.util.stream.Collectors;


/**
 * Orchestrates funds checks (optional) + matching engine.
 */
public class PlaceOrderUseCase {

    private final MatchingEngine engine;
    private final AccountService accountService;
    private final OrderRepository orderRepository;
    private final PositionRepository positionRepository;


    public PlaceOrderUseCase(final MatchingEngine engine, final AccountService accountService, final OrderRepository orderRepository, PositionRepository positionRepository) {
        this.engine = engine;
        this.accountService = accountService;
        this.orderRepository = orderRepository;
        this.positionRepository = positionRepository;
    }

    /**
     * Place an order and return trades executed (and high-level response)
     */
    public PlaceOrderResponse place(final PlaceOrderRequest req) {
        if (req.quantity <= 0) {
            return PlaceOrderResponse.fail("Quantity must be > 0.");
        }
        if (req.price != null && req.price <= 0) {
            return PlaceOrderResponse.fail("Price must be > 0 for limit orders.");
        }

        // optional funds check
        if (!accountService.hasSufficientFunds(req.userId, req.marketId, req.quantity, req.price)) {
            return PlaceOrderResponse.fail("Insufficient funds");
        }

        double calcPrice = (req.price == null ? 1.0 : req.price);
        double upfrontCost = calcPrice * req.quantity;

        if (accountService instanceof stakemate.service.DbAccountService dbAcc) {
            dbAcc.adjustBalance(req.userId, -upfrontCost);
        }


        // create internal order (price == null => market)
        final BookOrder incoming = new BookOrder(req.userId, req.marketId, req.side, req.price, req.quantity);
        accountService.reserveForOrder(req.userId, incoming.getId(), estimateReservationAmount(incoming));
        orderRepository.save(incoming);

        final List<Trade> trades = engine.placeOrder(incoming);
        System.out.println("DEBUG: trades.size = " + trades.size());

        final String msg = trades.isEmpty() ? "Order placed (no immediate trades)" : String.format("Executed %d trades", trades.size());
        return PlaceOrderResponse.success(msg);

    }

    private double estimateReservationAmount(final BookOrder o) {
        final double price = (o.getPrice() == null) ? 1.0 : o.getPrice();
        return price * o.getOriginalQty();
    }

    // expose engine snapshot for UI
    public stakemate.entity.OrderBook snapshot(final String marketId) {
        return engine.snapshotOrderBook(marketId);
    }

    public List<Trade> recentTrades() {
        return engine.getTrades();
    }

    /**
     * Return all open orders (bids + asks) for a given user.
     * This is used by the UI to populate the "Open Orders" table.
     */
    public List<BookOrder> openOrdersForUser(String userId) {
        return orderRepository.findOpenOrdersForUser(userId);
    }

}






