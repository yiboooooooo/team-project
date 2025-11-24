package stakemate.interface_adapter.view_comments;

import stakemate.use_case.comments.post.PostCommentInputData;
import stakemate.use_case.comments.post.PostCommentInputBoundary;

public class PostCommentController {

    private final PostCommentInputBoundary interactor;

    public PostCommentController(PostCommentInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void postComment(String marketId, String username, String message) {
        PostCommentInputData inputData = new PostCommentInputData(marketId, username, message);
        interactor.execute(inputData);
    }
}
