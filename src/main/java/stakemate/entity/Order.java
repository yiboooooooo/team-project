package stakemate.entity;

public class Order {

    private final String userId;
    private final Side side;
    private final double odds;
    private final double stake;

    public Order(final String userId, final Side side, final double odds, final double stake) {
        this.userId = userId;
        this.side = side;
        this.odds = odds;
        this.stake = stake;
    }

    public String getUserId() {
        return userId;
    }

    public Side getSide() {
        return side;
    }

    public double getOdds() {
        return odds;
    }

    public double getStake() {
        return stake;
    }

    @Override
    public String toString() {
        return "Order{" +
            "userId='" + userId + '\'' +
            ", side=" + side +
            ", odds=" + odds +
            ", stake=" + stake +
            '}';
    }

    public enum Side {BACK, LAY}
}
