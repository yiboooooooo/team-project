package stakemate.engine;

import java.time.Instant;
import java.util.UUID;

import stakemate.entity.Side;

/**
 * Internal (mutable) order representation used by the matching engine.
 */
public class BookOrder {
    private final String id;
    private final String userId;
    private final String marketId;
    private final Side side;
    private final Double price;
    private final double originalQty;
    private double remainingQty;
    private final Instant timestamp;

    public BookOrder(String id,
                     String userId,
                     String marketId,
                     Side side,
                     Double price,
                     double originalQty,
                     double remainingQty,
                     Instant timestamp) {

        this.id = id;
        this.userId = userId;
        this.marketId = marketId;
        this.side = side;
        this.price = price;
        this.originalQty = originalQty;
        this.remainingQty = remainingQty;
        this.timestamp = timestamp;
    }


    public BookOrder(final String userId, final String marketId, final Side side, final Double price, final double qty) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.marketId = marketId;
        this.side = side;
        this.price = price;
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

    public void reduce(final double filled) {
        remainingQty = Math.max(0.0, remainingQty - filled);
    }

    public boolean isFilled() {
        return remainingQty <= 0.0;
    }

    public boolean isMarket() {
        return price == null;
    }
}

