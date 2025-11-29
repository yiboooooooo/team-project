package stakemate.use_case.view_profile;

import stakemate.use_case.view_profile.strategy.BetComparator;
import stakemate.use_case.view_profile.strategy.DateBetComparator;

/**
 * Input Data for the View Profile Use Case.
 */
public class ViewProfileInputData {
    private final String username;
    private final BetComparator openSortStrategy;
    private final BetComparator historicalSortStrategy;

    /**
     * Constructs a ViewProfileInputData with specified sorting.
     * 
     * @param username               the username.
     * @param openSortStrategy       the sorting strategy for open positions.
     * @param historicalSortStrategy the sorting strategy for historical positions.
     */
    public ViewProfileInputData(final String username,
            final BetComparator openSortStrategy,
            final BetComparator historicalSortStrategy) {
        this.username = username;
        this.openSortStrategy = openSortStrategy;
        this.historicalSortStrategy = historicalSortStrategy;
    }

    /**
     * Constructs a ViewProfileInputData with default sorting (DATE).
     * 
     * @param username the username.
     */
    public ViewProfileInputData(final String username) {
        this(username, new DateBetComparator(), new DateBetComparator());
    }

    /**
     * Gets the username.
     * 
     * @return the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the sorting strategy for open positions.
     * 
     * @return the sorting strategy.
     */
    public BetComparator getOpenSortStrategy() {
        return openSortStrategy;
    }

    /**
     * Gets the sorting strategy for historical positions.
     * 
     * @return the sorting strategy.
     */
    public BetComparator getHistoricalSortStrategy() {
        return historicalSortStrategy;
    }
}
