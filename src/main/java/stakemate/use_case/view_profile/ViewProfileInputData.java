package stakemate.use_case.view_profile;

/**
 * Input Data for the View Profile Use Case.
 */
public class ViewProfileInputData {
    private final String username;
    private final SortCriteria openSortCriteria;
    private final SortCriteria historicalSortCriteria;

    /**
     * Constructs a ViewProfileInputData with specified sorting.
     * 
     * @param username               the username.
     * @param openSortCriteria       the sorting criteria for open positions.
     * @param historicalSortCriteria the sorting criteria for historical positions.
     */
    public ViewProfileInputData(final String username,
            final SortCriteria openSortCriteria,
            final SortCriteria historicalSortCriteria) {
        this.username = username;
        this.openSortCriteria = openSortCriteria;
        this.historicalSortCriteria = historicalSortCriteria;
    }

    /**
     * Constructs a ViewProfileInputData with default sorting (DATE).
     * 
     * @param username the username.
     */
    public ViewProfileInputData(final String username) {
        this(username, SortCriteria.DATE, SortCriteria.DATE);
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
     * Gets the sorting criteria for open positions.
     * 
     * @return the sorting criteria.
     */
    public SortCriteria getOpenSortCriteria() {
        return openSortCriteria;
    }

    /**
     * Gets the sorting criteria for historical positions.
     * 
     * @return the sorting criteria.
     */
    public SortCriteria getHistoricalSortCriteria() {
        return historicalSortCriteria;
    }
}
