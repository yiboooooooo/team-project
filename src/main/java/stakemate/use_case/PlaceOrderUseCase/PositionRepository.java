package stakemate.use_case.PlaceOrderUseCase;

import stakemate.engine.BookOrder;

public interface PositionRepository {
    /**
     * Save a position representing an executed portion of an order.
     * executedAmount = size filled in this trade for that order
     * executedPrice = trade price
     */
    void savePosition(BookOrder order, double executedAmount, double executedPrice);
}
