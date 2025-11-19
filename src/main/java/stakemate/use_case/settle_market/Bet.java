package stakemate.use_case.settle_market;

import stakemate.entity.Side;

public class Bet {

    private final String username;
    private final String marketId;
    private final Side side;
    private final double stake;
    private final double price;
    private boolean settled = false;
    private boolean won = false;

    public Bet(String username, String marketId, Side side, double stake, double price) {
        this.username = username;
        this.marketId = marketId;
        this.side = side;
        this.stake = stake;
        this.price = price;
    }

    public String getUsername() {
        return username;
    }

    public String getMarketId() {
        return marketId;
    }

    public Side getSide() {
        return side;
    }

    public double getStake() {
        return stake;
    }

    public double getPrice() {
        return price;
    }

    public boolean isSettled() {
        return settled;
    }

    public void setSettled(boolean settled) {
        this.settled = settled;
    }

    public boolean isWon() {
        return won;
    }

    public void setWon(boolean won) {
        this.won = won;
    }
}