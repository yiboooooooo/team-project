package stakemate.use_case.comments.view;

public class ViewCommentsInputData {
    private final String marketId;

    public ViewCommentsInputData(String marketId) {
        this.marketId = marketId;
    }

    public String getMarketId() {
        return marketId;
    }
}
