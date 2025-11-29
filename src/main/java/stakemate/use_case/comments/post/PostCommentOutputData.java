package stakemate.use_case.comments.post;

public class PostCommentOutputData {

    private final boolean success;
    private final String message;
    private final String marketId;

    public PostCommentOutputData(boolean success, String message, String marketId) {
        this.success = success;
        this.message = message;
        this.marketId = marketId;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getMarketId() { return marketId; }
}
