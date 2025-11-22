package stakemate.use_case.PlaceOrderUseCase;

public class PlaceOrderResponse {
    public final boolean ok;
    public final String message;

    private PlaceOrderResponse(final boolean ok, final String message) {
        this.ok = ok;
        this.message = message;
    }

    public static PlaceOrderResponse success(final String m) {
        return new PlaceOrderResponse(true, m);
    }

    public static PlaceOrderResponse fail(final String m) {
        return new PlaceOrderResponse(false, m);
    }
}

