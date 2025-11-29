package stakemate.interface_adapter.view_login;

import stakemate.use_case.login.LoginOutputBoundary;
import stakemate.use_case.login.LoginOutputData;

/**
 * Presenter for the Login Use Case (Swing implementation).
 */
public class SwingLoginPresenter implements LoginOutputBoundary {

    private final LoginView view;

    /**
     * Constructs a SwingLoginPresenter.
     * 
     * @param view the login view.
     */
    public SwingLoginPresenter(final LoginView view) {
        this.view = view;
    }

    @Override
    public void prepareSuccessView(final LoginOutputData data) {
        // Just tell the view it succeeded; the view will handle opening MarketsFrame.
        view.onLoginSuccess(data.getUsername());
    }

    @Override
    public void prepareFailView(final String error) {
        view.showError(error);
    }
}
