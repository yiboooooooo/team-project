package stakemate.use_case.comments.post;

/**
 * Output boundary for presenting the result of posting a comment.
 */
public interface PostCommentOutputBoundary {

    /**
     * Presents the output data after a comment is posted.
     *
     * @param outputData the data to present
     */
    void present(PostCommentOutputData outputData);
}
