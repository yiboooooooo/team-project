package stakemate.use_case.signup;

/**
 * Input Boundary for the Signup Use Case.
 */
public interface SignupInputBoundary {
    /**
     * Executes the signup use case.
     * 
     * @param inputData the input data.
     */
    void execute(SignupInputData inputData);
}
