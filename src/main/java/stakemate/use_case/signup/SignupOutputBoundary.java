package stakemate.use_case.signup;

/**
 * Output Boundary for the Signup Use Case.
 */
public interface SignupOutputBoundary {
    /**
     * Prepares the success view.
     * 
     * @param data the output data.
     */
    void prepareSuccessView(SignupOutputData data);

    /**
     * Prepares the failure view.
     * 
     * @param error the error message.
     */
    void prepareFailView(String error);
}
