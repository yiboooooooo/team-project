package stakemate.use_case.settle_market;

public class SettleMarketRequestModel {

    private final String marketId;
    private final boolean homeTeamWon;

    public SettleMarketRequestModel(String marketId, boolean homeTeamWon) {
        this.marketId = marketId;
        this.homeTeamWon = homeTeamWon;
    }

    public String getMarketId() {
        return marketId;
    }

    public boolean isHomeTeamWon() {
        return homeTeamWon;
    }
}
