package stakemate.use_case.settle_market;

public class SettleMarketResponseModel {

    private final String marketId;
    private final int betsSettled;
    private final double totalPayout;
    private final String settlementSummary; // <--- ADD THIS

    public SettleMarketResponseModel(final String marketId,
                                     final int betsSettled,
                                     final double totalPayout,
                                     final String settlementSummary) {
        this.marketId = marketId;
        this.betsSettled = betsSettled;
        this.totalPayout = totalPayout;
        this.settlementSummary = settlementSummary;
    }

    public String getMarketId() { return marketId; }
    public int getBetsSettled() { return betsSettled; }
    public double getTotalPayout() { return totalPayout; }
    public String getSettlementSummary() { return settlementSummary; } // <--- GETTER
}
