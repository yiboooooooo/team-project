package stakemate.use_case.settle_market;

public class SettleMarketRequestModel {

    private final String marketId;

    public SettleMarketRequestModel(String marketId) {
        this.marketId = marketId;
    }

    public String getMarketId() {
        return marketId;
    }
}
