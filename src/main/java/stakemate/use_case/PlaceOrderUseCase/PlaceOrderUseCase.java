package stakemate.use_case.PlaceOrderUseCase;


import stakemate.engine.BookOrder;
import stakemate.engine.MatchingEngine;
import stakemate.engine.Trade;
import stakemate.service.AccountService;

import java.util.List;

/**
 * Orchestrates funds checks (optional) + matching engine.
 */
public class PlaceOrderUseCase {

    private final MatchingEngine engine;
    private final AccountService accountService; // can be a no-op impl for demo

    public PlaceOrderUseCase(MatchingEngine engine, AccountService accountService) {
        this.engine = engine;
        this.accountService = accountService;
    }

    /**
     * Place an order and return trades executed (and high-level response)
     */
    public PlaceOrderResponse place(PlaceOrderRequest req) {
        if (req.quantity <= 0) return PlaceOrderResponse.fail("Quantity must be > 0.");
        if (req.price != null && req.price <= 0) return PlaceOrderResponse.fail("Price must be > 0 for limit orders.");

        // optional funds check
        if (!accountService.hasSufficientFunds(req.userId, req.marketId, req.quantity, req.price)) {
            return PlaceOrderResponse.fail("Insufficient funds");
        }

        // create internal order (price == null => market)
        BookOrder incoming = new BookOrder(req.userId, req.marketId, req.side, req.price, req.quantity);
        // reserve funds for the order (demo best-effort)
        accountService.reserveForOrder(req.userId, incoming.getId(), estimateReservationAmount(incoming));

        List<Trade> trades = engine.placeOrder(incoming);

        // after trades, capture funds for matched trades (demo best-effort)
        for (Trade t : trades) {
            // naive settlement: buyer pays price*size to seller
            accountService.capture(t);
        }

        String msg = trades.isEmpty() ? "Order placed (no immediate trades)" : String.format("Executed %d trades", trades.size());
        return PlaceOrderResponse.success(msg);
    }

    private double estimateReservationAmount(BookOrder o) {
        double price = (o.getPrice() == null) ? 1.0 : o.getPrice();
        return price * o.getOriginalQty();
    }

    // expose engine snapshot for UI
    public stakemate.entity.OrderBook snapshot(String marketId) {
        return engine.snapshotOrderBook(marketId);
    }

    public List<Trade> recentTrades() {
        return engine.getTrades();
    }
}

