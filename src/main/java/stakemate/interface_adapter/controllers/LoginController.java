package stakemate.interface_adapter.controllers;

import stakemate.use_case.login.LoginInputBoundary;
import stakemate.use_case.login.LoginInputData;

public class LoginController {
    private final LoginInputBoundary interactor;

    public LoginController(LoginInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(String username, String password) {
        LoginInputData inputData = new LoginInputData(username, password);
        interactor.execute(inputData);
    }
}
