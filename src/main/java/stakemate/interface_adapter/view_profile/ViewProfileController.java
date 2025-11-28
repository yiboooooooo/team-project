package stakemate.interface_adapter.view_profile;

import stakemate.use_case.view_profile.ViewProfileInputBoundary;
import stakemate.use_case.view_profile.ViewProfileInputData;

public class ViewProfileController {
    private final ViewProfileInputBoundary interactor;

    public ViewProfileController(final ViewProfileInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(final String username) {
        execute(username,
                stakemate.use_case.view_profile.SortCriteria.DATE,
                stakemate.use_case.view_profile.SortCriteria.DATE);
    }

    public void execute(final String username,
            final stakemate.use_case.view_profile.SortCriteria openSort,
            final stakemate.use_case.view_profile.SortCriteria historicalSort) {
        final ViewProfileInputData inputData = new ViewProfileInputData(username, openSort, historicalSort);
        interactor.execute(inputData);
    }
}
