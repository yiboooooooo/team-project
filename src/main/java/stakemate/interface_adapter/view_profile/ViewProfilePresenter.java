package stakemate.interface_adapter.view_profile;

import stakemate.use_case.view_profile.ViewProfileOutputBoundary;
import stakemate.use_case.view_profile.ViewProfileOutputData;

public class ViewProfilePresenter implements ViewProfileOutputBoundary {
    private final ProfileViewModel viewModel;

    public ViewProfilePresenter(final ProfileViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentProfile(final ViewProfileOutputData outputData) {
        final ProfileState state = viewModel.getState();
        state.setUsername(outputData.getUsername());
        state.setBalance(outputData.getBalance());
        state.setPnl(outputData.getPnl());
        state.setOpenPositions(outputData.getOpenPositions());
        state.setHistoricalPositions(outputData.getHistoricalPositions());
        state.setError(null);

        viewModel.setState(state);
        viewModel.firePropertyChanged();
    }

    @Override
    public void presentError(final String error) {
        final ProfileState state = viewModel.getState();
        state.setError(error);
        viewModel.setState(state);
        viewModel.firePropertyChanged();
    }
}
