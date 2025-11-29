package stakemate.use_case.view_profile;

import java.util.List;
import stakemate.use_case.settle_market.Bet;

/**
 * Output Data for the View Profile Use Case.
 */
public class ViewProfileOutputData {
    private final String username;
    private final double balance;
    private final double pnl;
    private final List<Bet> openPositions;
    private final List<Bet> historicalPositions;

    /**
     * Constructs a ViewProfileOutputData.
     * 
     * @param username            the username.
     * @param balance             the balance.
     * @param pnl                 the pnl.
     * @param openPositions       the open positions.
     * @param historicalPositions the historical positions.
     */
    public ViewProfileOutputData(final String username,
            final double balance,
            final double pnl,
            final List<Bet> openPositions,
            final List<Bet> historicalPositions) {
        this.username = username;
        this.balance = balance;
        this.pnl = pnl;
        this.openPositions = openPositions;
        this.historicalPositions = historicalPositions;
    }

    /**
     * Gets the username.
     * 
     * @return the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the balance.
     * 
     * @return the balance.
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Gets the PnL.
     * 
     * @return the PnL.
     */
    public double getPnl() {
        return pnl;
    }

    /**
     * Gets the open positions.
     * 
     * @return the open positions.
     */
    public List<Bet> getOpenPositions() {
        return openPositions;
    }

    /**
     * Gets the historical positions.
     * 
     * @return the historical positions.
     */
    public List<Bet> getHistoricalPositions() {
        return historicalPositions;
    }
}
