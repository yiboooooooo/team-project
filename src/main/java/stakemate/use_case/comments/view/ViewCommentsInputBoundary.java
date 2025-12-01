package stakemate.use_case.comments.view;

/**
 * Input boundary for viewing comments.
 */
public interface ViewCommentsInputBoundary {

    /**
     * Executes the use case for viewing comments.
     *
     * @param inputData the input data containing the market ID
     */
    void execute(ViewCommentsInputData inputData);
}
