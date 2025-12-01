package stakemate.use_case.comments.post;

public class PostCommentInputData {

    private final String marketId;
    private final String username;
    private final String commentText;

    public PostCommentInputData(String marketId, String username, String commentText) {
        this.marketId = marketId;
        this.username = username;
        this.commentText = commentText;
    }

    public String getMarketId() {
        return marketId;
    }

    public String getUsername() {
        return username;
    }

    public String getCommentText() {
        return commentText;
    }
}
