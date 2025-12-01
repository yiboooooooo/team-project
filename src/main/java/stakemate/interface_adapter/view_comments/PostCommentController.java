package stakemate.interface_adapter.view_comments;

import stakemate.use_case.comments.post.PostCommentInputBoundary;
import stakemate.use_case.comments.post.PostCommentInputData;

public class PostCommentController {

    private final PostCommentInputBoundary interactor;

    public PostCommentController(PostCommentInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * Sends a request to post a comment for the given market.
     *
     * @param marketId the market the comment belongs to
     * @param username the user posting the comment
     * @param message the comment text
     */
    public void postComment(String marketId, String username, String message) {
        final PostCommentInputData inputData = new PostCommentInputData(marketId, username, message);
        interactor.execute(inputData);
    }
}
