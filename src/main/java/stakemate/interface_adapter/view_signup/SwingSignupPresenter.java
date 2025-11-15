package stakemate.interface_adapter.view_signup;

import stakemate.use_case.signup.SignupOutputBoundary;
import stakemate.use_case.signup.SignupOutputData;

public class SwingSignupPresenter implements SignupOutputBoundary {

    private final SignupView view;

    public SwingSignupPresenter(SignupView view) {
        this.view = view;
    }

    @Override
    public void prepareSuccessView(SignupOutputData data) {
        view.onSignupSuccess(data.getUsername());
    }

    @Override
    public void prepareFailView(String error) {
        view.showError(error);
    }
}
