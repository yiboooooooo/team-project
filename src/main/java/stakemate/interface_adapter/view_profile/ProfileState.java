package stakemate.interface_adapter.view_profile;

import java.util.ArrayList;
import java.util.List;

/**
 * State for the Profile View.
 */
public class ProfileState {
    private String username = "";
    private double balance = 0;
    private double pnl = 0;
    private List<String[]> openPositions = new ArrayList<>();
    private List<String[]> historicalPositions = new ArrayList<>();
    private String error = null;

    /**
     * Constructs a new ProfileState.
     */
    public ProfileState() {
    }

    /**
     * Constructs a copy of an existing ProfileState.
     * 
     * @param copy the state to copy.
     */
    public ProfileState(final ProfileState copy) {
        this.username = copy.username;
        this.balance = copy.balance;
        this.pnl = copy.pnl;
        this.openPositions = copy.openPositions;
        this.historicalPositions = copy.historicalPositions;
        this.error = copy.error;
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
     * Sets the username.
     * 
     * @param username the username to set.
     */
    public void setUsername(final String username) {
        this.username = username;
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
     * Sets the balance.
     * 
     * @param balance the balance to set.
     */
    public void setBalance(final double balance) {
        this.balance = balance;
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
     * Sets the PnL.
     * 
     * @param pnl the PnL to set.
     */
    public void setPnl(final double pnl) {
        this.pnl = pnl;
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
     * Sets the list of open positions.
     * 
     * @param openPositions the list of open positions to set.
     */
    public void setOpenPositions(final List<String[]> openPositions) {
        this.openPositions = openPositions;
    }

    /**
     * Gets the list of historical positions.
     * 
     * @return the list of historical positions.
     */
    public List<String[]> getHistoricalPositions() {
        return historicalPositions;
    }

    /**
     * Sets the list of historical positions.
     * 
     * @param historicalPositions the list of historical positions to set.
     */
    public void setHistoricalPositions(final List<String[]> historicalPositions) {
        this.historicalPositions = historicalPositions;
    }

    /**
     * Gets the error message.
     * 
     * @return the error message.
     */
    public String getError() {
        return error;
    }

    /**
     * Sets the error message.
     * 
     * @param error the error message to set.
     */
    public void setError(final String error) {
        this.error = error;
    }
}
