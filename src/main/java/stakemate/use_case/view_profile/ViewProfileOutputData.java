package stakemate.use_case.view_profile;

import java.util.List;

/**
 * Output Data for the View Profile Use Case.
 */
public class ViewProfileOutputData {
    private final String username;
    private final int balance;
    private final int pnl;
    private final List<String[]> openPositions;
    private final List<String[]> historicalPositions;

    /**
     * Constructs a ViewProfileOutputData.
     * 
     * @param username            the username.
     * @param balance             the balance.
     * @param pnl                 the PnL.
     * @param openPositions       the list of open positions.
     * @param historicalPositions the list of historical positions.
     */
    public ViewProfileOutputData(final String username, final int balance, final int pnl,
            final List<String[]> openPositions,
            final List<String[]> historicalPositions) {
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
    public int getBalance() {
        return balance;
    }

    /**
     * Gets the PnL.
     * 
     * @return the PnL.
     */
    public int getPnl() {
        return pnl;
    }

    /**
     * Gets the list of open positions.
     * 
     * @return the list of open positions.
     */
    public List<String[]> getOpenPositions() {
        return openPositions;
    }

    /**
     * Gets the list of historical positions.
     * 
     * @return the list of historical positions.
     */
    public List<String[]> getHistoricalPositions() {
        return historicalPositions;
    }
}
