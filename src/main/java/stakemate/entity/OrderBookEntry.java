package stakemate.entity;

public class OrderBookEntry {
    private final Side side;
    private final double price;
    private final double quantity;

    public OrderBookEntry(Side side, double price, double quantity) {
        this.side = side;
        this.price = price;
        this.quantity = quantity;
    }

    public Side getSide() {
        return side;
    }

    public double getPrice() {
        return price;
    }

    public double getQuantity() {
        return quantity;
    }
}
