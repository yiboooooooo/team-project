package stakemate.use_case.PlaceOrderUseCase;

import stakemate.entity.Side;

public class PlaceOrderRequest {

    public final String userId;
    public final String marketId;
    public final Side side;
    public final double quantity;
    public final Double price;

    public PlaceOrderRequest(String userId,
                             String marketId,
                             Side side,
                             double quantity,
                             Double price) {
        this.userId = userId;
        this.marketId = marketId;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
    }
}

