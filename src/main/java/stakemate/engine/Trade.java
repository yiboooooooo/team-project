package stakemate.engine;

import java.time.Instant;
import java.util.UUID;

public class Trade {
    private final String id;
    private final String marketId;
    private final String buyOrderId;
    private final String sellOrderId;
    private final double price;
    private final double size;
    private final Instant timestamp;

    public Trade(String marketId, String buyOrderId, String sellOrderId, double price, double size) {
        this.id = UUID.randomUUID().toString();
        this.marketId = marketId;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.price = price;
        this.size = size;
        this.timestamp = Instant.now();
    }

    public String getId() { return id; }
    public String getMarketId() { return marketId; }
    public String getBuyOrderId() { return buyOrderId; }
    public String getSellOrderId() { return sellOrderId; }
    public double getPrice() { return price; }
    public double getSize() { return size; }
    public Instant getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] TRADE %s BUY:%s SELL:%s @ %.2f x %.2f",
                timestamp, id, buyOrderId, sellOrderId, price, size);
    }
}
