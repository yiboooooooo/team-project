package stakemate.entity;

public class Order {

    public enum Side { BACK, LAY }

    private String userId;
    private Side side;
    private double odds;
    private double stake;

    public Order(String userId, Side side, double odds, double stake) {
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
}
