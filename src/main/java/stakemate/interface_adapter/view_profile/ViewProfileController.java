package stakemate.interface_adapter.view_profile;

import stakemate.use_case.view_profile.SortCriteria;
import stakemate.use_case.view_profile.ViewProfileInputBoundary;
import stakemate.use_case.view_profile.ViewProfileInputData;
import stakemate.use_case.view_profile.strategy.BetComparator;
import stakemate.use_case.view_profile.strategy.DateBetComparator;
import stakemate.use_case.view_profile.strategy.SizeBetComparator;

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
     * Executes the View Profile Use Case.
     * 
     * @param username               the username.
     * @param openSortCriteria       the sorting criteria for open positions.
     * @param historicalSortCriteria the sorting criteria for historical positions.
     */
    public void execute(final String username,
            final SortCriteria openSortCriteria,
            final SortCriteria historicalSortCriteria) {
        final BetComparator openStrategy = getStrategy(openSortCriteria);
        final BetComparator histStrategy = getStrategy(historicalSortCriteria);

        final ViewProfileInputData inputData = new ViewProfileInputData(
                username,
                openStrategy,
                histStrategy);

        interactor.execute(inputData);
    }

    /**
     * Executes the View Profile Use Case with default sorting (DATE).
     * 
     * @param username the username.
     */
    public void execute(final String username) {
        execute(username, SortCriteria.DATE, SortCriteria.DATE);
    }

    private BetComparator getStrategy(final SortCriteria criteria) {
        if (criteria == SortCriteria.SIZE) {
            return new SizeBetComparator();
        }
        // Default to Date
        return new DateBetComparator();
    }
}
