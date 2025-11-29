package stakemate.use_case.view_profile;

/**
 * Input Boundary for the View Profile Use Case.
 */
public interface ViewProfileInputBoundary {
    /**
     * Executes the view profile use case.
     * 
     * @param inputData the input data.
     */
    void execute(ViewProfileInputData inputData);
}
