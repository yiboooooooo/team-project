package stakemate.use_case.PlaceOrderUseCase;

import stakemate.entity.Side;

/**
 * Simple DTO used by UI/controller to place an order.
 */
public class PlaceOrderRequest {
    public final String userId;
    public final String marketId;
    public final Side side;
    public final Double price;
    public final double quantity;

    public PlaceOrderRequest(final String userId, final String marketId, final Side side, final Double price, final double quantity) {
        this.userId = userId;
        this.marketId = marketId;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
    }
}
