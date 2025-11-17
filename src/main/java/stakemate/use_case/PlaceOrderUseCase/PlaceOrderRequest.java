package stakemate.use_case.PlaceOrderUseCase;

import stakemate.entity.Side;

/**
 * Simple DTO used by UI/controller to place an order.
 */
public class PlaceOrderRequest {
    public final String userId;
    public final String marketId;
    public final Side side;
    public final Double price; // null for market orders
    public final double quantity;

    public PlaceOrderRequest(String userId, String marketId, Side side, Double price, double quantity) {
        this.userId = userId;
        this.marketId = marketId;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
    }
}
