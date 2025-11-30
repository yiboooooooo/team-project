package stakemate.use_case.PlaceOrderUseCase;

import stakemate.engine.BookOrder;
import stakemate.entity.Side;

import java.util.List;

public interface OrderRepository {
    void save(BookOrder order);

    BookOrder findById(String orderId);

    /**
     * All open orders (remaining_qty > 0) for a market & side.
     */
    List<BookOrder> findOpenOrdersForMarket(String marketId, Side side);

    /**
     * All open orders (remaining_qty > 0) belonging to a user.
     * Used for "Open Orders" in the UI.
     */
    List<BookOrder> findOpenOrdersForUser(String userId);

    /**
     * Persist a new remaining_qty for a given order.
     */
    void updateRemainingQty(String orderId, double newRemainingQty);

    void reduceRemainingQty(String orderId, double newRemainingQty);
    List<BookOrder> findOppositeSideOrders(String marketId, Side incomingSide);

}


