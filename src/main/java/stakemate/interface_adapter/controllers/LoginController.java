package stakemate.interface_adapter.controllers;

import stakemate.use_case.login.LoginInputBoundary;
import stakemate.use_case.login.LoginInputData;

/**
 * Controller for the Login Use Case.
 */
public class LoginController {
    private final LoginInputBoundary interactor;

    /**
     * Constructs a LoginController.
     * 
     * @param interactor the login interactor.
     */
    public LoginController(final LoginInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * Executes the login use case.
     * 
     * @param username the username.
     * @param password the password.
     */
    public void execute(final String username, final String password) {
        final LoginInputData inputData = new LoginInputData(username, password);
        interactor.execute(inputData);
    }
}
