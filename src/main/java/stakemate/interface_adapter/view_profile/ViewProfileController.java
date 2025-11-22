package stakemate.interface_adapter.view_profile;

import stakemate.use_case.view_profile.ViewProfileInputBoundary;
import stakemate.use_case.view_profile.ViewProfileInputData;

public class ViewProfileController {
    private final ViewProfileInputBoundary interactor;

    public ViewProfileController(final ViewProfileInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(final String username) {
        final ViewProfileInputData inputData = new ViewProfileInputData(username);
        interactor.execute(inputData);
    }
}
