package stakemate.interface_adapter.view_signup;

import stakemate.use_case.signup.SignupInputBoundary;
import stakemate.use_case.signup.SignupInputData;

/**
 * Controller for the Signup Use Case.
 */
public class SignupController {
    private final SignupInputBoundary interactor;

    /**
     * Constructs a SignupController.
     * 
     * @param interactor the signup interactor.
     */
    public SignupController(final SignupInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * Executes the signup use case.
     * 
     * @param username the username.
     * @param password the password.
     */
    public void execute(final String username, final String password) {
        final SignupInputData inputData = new SignupInputData(username, password);
        interactor.execute(inputData);
    }
}
