package stakemate.interface_adapter.view_login;

import stakemate.use_case.login.LoginOutputBoundary;
import stakemate.use_case.login.LoginOutputData;

/**
 * Presenter for the Login Use Case (Swing implementation).
 */
public class SwingLoginPresenter implements LoginOutputBoundary {

    private final LoginViewModel viewModel;

    /**
     * Constructs a SwingLoginPresenter.
     * 
     * @param viewModel the login view model.
     */
    public SwingLoginPresenter(final LoginViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(final LoginOutputData data) {
        final LoginState state = viewModel.getState();
        state.setUsername(data.getUsername());
        state.setError(null);
        viewModel.setState(state);
    }

    @Override
    public void prepareFailView(final String error) {
        final LoginState state = viewModel.getState();
        state.setError(error);
        viewModel.setState(state);
    }
}
