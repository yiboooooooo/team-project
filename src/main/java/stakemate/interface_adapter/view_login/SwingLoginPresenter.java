package stakemate.interface_adapter.view_login;

import stakemate.use_case.login.LoginOutputBoundary;
import stakemate.use_case.login.LoginOutputData;

public class SwingLoginPresenter implements LoginOutputBoundary {

    private final LoginView view;

    public SwingLoginPresenter(LoginView view) {
        this.view = view;
    }

    @Override
    public void prepareSuccessView(LoginOutputData data) {
        // Just tell the view it succeeded; the view will handle opening MarketsFrame.
        view.onLoginSuccess(data.getUsername());
    }

    @Override
    public void prepareFailView(String error) {
        view.showError(error);
    }
}
