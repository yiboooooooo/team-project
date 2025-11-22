package stakemate.interface_adapter.view_profile;

import java.util.ArrayList;
import java.util.List;

public class ProfileState {
    private String username = "";
    private int balance = 0;
    private int pnl = 0;
    private List<String[]> openPositions = new ArrayList<>();
    private List<String[]> historicalPositions = new ArrayList<>();
    private String error = null;

    public ProfileState() {
    }

    public ProfileState(final ProfileState copy) {
        this.username = copy.username;
        this.balance = copy.balance;
        this.pnl = copy.pnl;
        this.openPositions = copy.openPositions;
        this.historicalPositions = copy.historicalPositions;
        this.error = copy.error;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(final int balance) {
        this.balance = balance;
    }

    public int getPnl() {
        return pnl;
    }

    public void setPnl(final int pnl) {
        this.pnl = pnl;
    }

    public List<String[]> getOpenPositions() {
        return openPositions;
    }

    public void setOpenPositions(final List<String[]> openPositions) {
        this.openPositions = openPositions;
    }

    public List<String[]> getHistoricalPositions() {
        return historicalPositions;
    }

    public void setHistoricalPositions(final List<String[]> historicalPositions) {
        this.historicalPositions = historicalPositions;
    }

    public String getError() {
        return error;
    }

    public void setError(final String error) {
        this.error = error;
    }
}
