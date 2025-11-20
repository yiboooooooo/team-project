package stakemate.use_case.settle_market;

public class SettleMarketResponseModel {

    private final String marketId;
    private final int betsSettled;
    private final double totalPayout;

    public SettleMarketResponseModel(final String marketId,
                                     final int betsSettled,
                                     final double totalPayout) {
        this.marketId = marketId;
        this.betsSettled = betsSettled;
        this.totalPayout = totalPayout;
    }

    public String getMarketId() {
        return marketId;
    }

    public int getBetsSettled() {
        return betsSettled;
    }

    public double getTotalPayout() {
        return totalPayout;
    }
}
