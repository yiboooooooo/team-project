package stakemate.use_case.comments;

public class PostCommentInputData {
    private final String marketId;
    private final String username;
    private final String message;

    public PostCommentInputData(String marketId, String username, String message) {
        this.marketId = marketId;
        this.username = username;
        this.message = message;
    }

    public String getMarketId() { return marketId; }
    public String getUsername() { return username; }
    public String getMessage() { return message; }
}
