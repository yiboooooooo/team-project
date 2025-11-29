package stakemate.use_case.login;

/**
 * Input Boundary for the Login Use Case.
 */
public interface LoginInputBoundary {
    /**
     * Executes the login use case.
     * 
     * @param inputData the input data.
     */
    void execute(LoginInputData inputData);
}
