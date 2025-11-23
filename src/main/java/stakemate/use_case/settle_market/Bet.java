package stakemate.use_case.settle_market;

import stakemate.entity.Side;

/**
 * Represents a single bet/position for a user on a specific market.
 */
public class Bet {

    private final String username;
    private final String marketId;
    private final Side side;
    private final double stake;   // corresponds to positions.amount
    private final double price;   // price/odds
    private final Boolean won;    // true = won, false = lost, null = not settled yet
    private final Boolean settled; // true = already settled, false = not settled

    public Bet(String username,
               String marketId,
               Side side,
               double stake,
               double price,
               Boolean won,
               Boolean settled) {

        this.username = username;
        this.marketId = marketId;
        this.side = side;
        this.stake = stake;
        this.price = price;
        this.won = won;
        this.settled = settled;
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

    public Boolean isWon() {
        return won;
    }

    public Boolean isSettled() {
        return settled;
    }
}
