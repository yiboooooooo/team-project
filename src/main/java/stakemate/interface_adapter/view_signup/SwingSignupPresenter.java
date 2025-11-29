package stakemate.interface_adapter.view_signup;

import stakemate.use_case.signup.SignupOutputBoundary;
import stakemate.use_case.signup.SignupOutputData;

/**
 * Presenter for the Signup Use Case (Swing implementation).
 */
public class SwingSignupPresenter implements SignupOutputBoundary {

    private final SignupView view;

    /**
     * Constructs a SwingSignupPresenter.
     * 
     * @param view the signup view.
     */
    public SwingSignupPresenter(final SignupView view) {
        this.view = view;
    }

    @Override
    public void prepareSuccessView(final SignupOutputData data) {
        view.onSignupSuccess(data.getUsername());
    }

    @Override
    public void prepareFailView(final String error) {
        view.showError(error);
    }
}
