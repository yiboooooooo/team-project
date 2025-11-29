package stakemate.use_case.PlaceOrderUseCase;

import java.util.List;

import stakemate.engine.BookOrder;
import stakemate.engine.MatchingEngine;
import stakemate.engine.Trade;
import stakemate.service.AccountService;

/**
 * Orchestrates funds checks (optional) + matching engine.
 * 
 * Updated to support both In-Memory (legacy) and DB-backed (Postgres) modes.
 */
public class PlaceOrderUseCase {

    private final MatchingEngine engine;
    private final AccountService accountService;

    // --- DB Fields ---
    private OrderRepository orderRepository;
    private PositionRepository positionRepository;

    /**
     * Constructor for In-Memory mode.
     */
    public PlaceOrderUseCase(final MatchingEngine engine, final AccountService accountService) {
        this.engine = engine;
        this.accountService = accountService;
    }

    /**
     * Constructor for DB-backed mode.
     */
    public PlaceOrderUseCase(final MatchingEngine engine, final AccountService accountService,
            final OrderRepository orderRepository, PositionRepository positionRepository) {
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

        // --- DB Specific: Upfront cost deduction ---
        if (orderRepository != null && req.price != null) {
            double calcPrice = req.price;
            double upfrontCost = calcPrice * req.quantity;

            if (accountService instanceof stakemate.service.DbAccountService) {
                stakemate.service.DbAccountService dbAcc = (stakemate.service.DbAccountService) accountService;
                dbAcc.adjustBalance(req.userId, -upfrontCost);
            }
        }

        // create internal order (price == null => market)
        final BookOrder incoming = new BookOrder(req.userId, req.marketId, req.side, req.price, req.quantity);
        accountService.reserveForOrder(req.userId, incoming.getId(), estimateReservationAmount(incoming));

        // --- DB Specific: Save order ---
        if (orderRepository != null) {
            orderRepository.save(incoming);
        }

        final List<Trade> trades = engine.placeOrder(incoming);

        // --- In-Memory Specific: Naive settlement ---
        if (orderRepository == null) {
            for (final Trade t : trades) {
                // naive settlement: buyer pays price*size to seller
                accountService.capture(t);
            }
        }

        final String msg = trades.isEmpty() ? "Order placed (no immediate trades)"
                : String.format("Executed %d trades", trades.size());
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
        if (orderRepository != null) {
            return orderRepository.findOpenOrdersForUser(userId);
        }
        // In-memory fallback (not implemented/needed for demo)
        return java.util.Collections.emptyList();
    }
}
