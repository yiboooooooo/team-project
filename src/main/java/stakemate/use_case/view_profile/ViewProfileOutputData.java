package stakemate.use_case.view_profile;

import java.util.List;

public class ViewProfileOutputData {
    private final String username;
    private final int balance;
    private final int pnl;
    private final List<String[]> openPositions;
    private final List<String[]> historicalPositions;

    public ViewProfileOutputData(String username, int balance, int pnl,
            List<String[]> openPositions,
            List<String[]> historicalPositions) {
        this.username = username;
        this.balance = balance;
        this.pnl = pnl;
        this.openPositions = openPositions;
        this.historicalPositions = historicalPositions;
    }

    public String getUsername() {
        return username;
    }

    public int getBalance() {
        return balance;
    }

    public int getPnl() {
        return pnl;
    }

    public List<String[]> getOpenPositions() {
        return openPositions;
    }

    public List<String[]> getHistoricalPositions() {
        return historicalPositions;
    }
}
