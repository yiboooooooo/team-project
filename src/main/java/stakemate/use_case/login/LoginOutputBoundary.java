package stakemate.use_case.login;

/**
 * Output Boundary for the Login Use Case.
 */
public interface LoginOutputBoundary {
    /**
     * Prepares the success view.
     * 
     * @param data the output data.
     */
    void prepareSuccessView(LoginOutputData data);

    /**
     * Prepares the failure view.
     * 
     * @param error the error message.
     */
    void prepareFailView(String error);
}
