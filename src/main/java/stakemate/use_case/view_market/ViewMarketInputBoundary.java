package stakemate.use_case.view_market;

public interface ViewMarketInputBoundary {
    void loadMatches();
    void matchSelected(String matchId);
    void marketSelected(String marketId);
}
