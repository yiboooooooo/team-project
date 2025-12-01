package stakemate.use_case.comments.post;

/**
 * Output data returned after posting a comment.
 */
public class PostCommentOutputData {

    private final boolean success;
    private final String message;
    private final String marketId;

    /**
     * Creates output data for posting a comment.
     *
     * @param success  whether the operation succeeded
     * @param message  the related message
     * @param marketId the market ID of the comment
     */
    public PostCommentOutputData(boolean success, String message, String marketId) {
        this.success = success;
        this.message = message;
        this.marketId = marketId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getMarketId() {
        return marketId;
    }
}
