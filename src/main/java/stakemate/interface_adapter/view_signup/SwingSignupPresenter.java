package stakemate.interface_adapter.view_signup;

import stakemate.use_case.signup.SignupOutputBoundary;
import stakemate.use_case.signup.SignupOutputData;

/**
 * Presenter for the Signup Use Case (Swing implementation).
 */
public class SwingSignupPresenter implements SignupOutputBoundary {

    private final SignupViewModel viewModel;

    /**
     * Constructs a SwingSignupPresenter.
     * 
     * @param viewModel the signup view model.
     */
    public SwingSignupPresenter(final SignupViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(final SignupOutputData data) {
        final SignupState state = viewModel.getState();
        state.setUsername(data.getUsername());
        state.setError(null);
        viewModel.setState(state);
    }

    @Override
    public void prepareFailView(final String error) {
        final SignupState state = viewModel.getState();
        state.setError(error);
        viewModel.setState(state);
    }
}
