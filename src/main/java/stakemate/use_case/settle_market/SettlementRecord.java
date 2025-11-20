package stakemate.use_case.settle_market;

import java.time.LocalDateTime;

/**
 * A record describing how a user's bet on a market was settled.
 * This is stored for auditing and demo purposes.
 */
public class SettlementRecord {

    private final String marketId;
    private final String username;
    private final double stake;
    private final double payout;
    private final boolean won;
    private final LocalDateTime settledAt;

    public SettlementRecord(final String marketId,
                            final String username,
                            final double stake,
                            final double payout,
                            final boolean won,
                            final LocalDateTime settledAt) {

        this.marketId = marketId;
        this.username = username;
        this.stake = stake;
        this.payout = payout;
        this.won = won;
        this.settledAt = settledAt;
    }

    public String getMarketId() {
        return marketId;
    }

    public String getUsername() {
        return username;
    }

    public double getStake() {
        return stake;
    }

    public double getPayout() {
        return payout;
    }

    public boolean isWon() {
        return won;
    }

    public LocalDateTime getSettledAt() {
        return settledAt;
    }
}
