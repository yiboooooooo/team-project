package stakemate.use_case.comments.post;

/**
 * Input boundary for posting a comment.
 */
public interface PostCommentInputBoundary {

    /**
     * Executes the use case to post a comment.
     *
     * @param inputData the input data for posting a comment
     */
    void execute(PostCommentInputData inputData);
}
