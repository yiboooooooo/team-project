package stakemate.engine;

import stakemate.entity.Side;

import java.time.Instant;
import java.util.UUID;

/**
 * Internal (mutable) order representation used by the matching engine.
 */
public class BookOrder {
    private final String id;
    private final String userId;
    private final String marketId;
    private final Side side;
    private final Double price; // null for market orders
    private final Instant timestamp;

    private final double originalQty;
    private double remainingQty;

    public BookOrder(String userId, String marketId, Side side, Double price, double qty) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.marketId = marketId;
        this.side = side;
        this.price = price; // null if market
        this.originalQty = qty;
        this.remainingQty = qty;
        this.timestamp = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getMarketId() {
        return marketId;
    }

    public Side getSide() {
        return side;
    }

    public Double getPrice() {
        return price;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public double getOriginalQty() {
        return originalQty;
    }

    public double getRemainingQty() {
        return remainingQty;
    }

    public void reduce(double filled) {
        remainingQty = Math.max(0.0, remainingQty - filled);
    }

    public boolean isFilled() {
        return remainingQty <= 0.0;
    }

    public boolean isMarket() {
        return price == null;
    }
}
