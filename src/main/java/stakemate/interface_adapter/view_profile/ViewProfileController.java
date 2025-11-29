package stakemate.interface_adapter.view_profile;

import stakemate.use_case.view_profile.ViewProfileInputBoundary;
import stakemate.use_case.view_profile.ViewProfileInputData;

/**
 * Controller for the View Profile Use Case.
 */
public class ViewProfileController {
    private final ViewProfileInputBoundary interactor;

    /**
     * Constructs a ViewProfileController.
     * 
     * @param interactor the interactor.
     */
    public ViewProfileController(final ViewProfileInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * Executes the view profile use case with default sorting.
     * 
     * @param username the username.
     */
    public void execute(final String username) {
        execute(username,
                stakemate.use_case.view_profile.SortCriteria.DATE,
                stakemate.use_case.view_profile.SortCriteria.DATE);
    }

    /**
     * Executes the view profile use case with specified sorting.
     * 
     * @param username       the username.
     * @param openSort       the sorting criteria for open positions.
     * @param historicalSort the sorting criteria for historical positions.
     */
    public void execute(final String username,
            final stakemate.use_case.view_profile.SortCriteria openSort,
            final stakemate.use_case.view_profile.SortCriteria historicalSort) {
        final ViewProfileInputData inputData = new ViewProfileInputData(username, openSort, historicalSort);
        interactor.execute(inputData);
    }
}
