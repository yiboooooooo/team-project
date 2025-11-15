package stakemate.interface_adapter.controllers;

import stakemate.use_case.signup.SignupInputBoundary;
import stakemate.use_case.signup.SignupInputData;

public class SignupController {
    private final SignupInputBoundary interactor;

    public SignupController(SignupInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(String username, String password) {
        SignupInputData inputData = new SignupInputData(username, password);
        interactor.execute(inputData);
    }
}
