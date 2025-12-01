package stakemate.use_case.comments.view;

/**
 * Output boundary for returning comment view results to the presenter.
 */
public interface ViewCommentsOutputBoundary {

    /**
     * Presents the results of viewing comments.
     *
     * @param outputData the data to present
     */
    void present(ViewCommentsOutputData outputData);
}
