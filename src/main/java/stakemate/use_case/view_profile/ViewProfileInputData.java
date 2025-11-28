package stakemate.use_case.view_profile;

public class ViewProfileInputData {
    private final String username;
    private final SortCriteria openSortCriteria;
    private final SortCriteria historicalSortCriteria;

    public ViewProfileInputData(final String username,
            final SortCriteria openSortCriteria,
            final SortCriteria historicalSortCriteria) {
        this.username = username;
        this.openSortCriteria = openSortCriteria;
        this.historicalSortCriteria = historicalSortCriteria;
    }

    public ViewProfileInputData(final String username) {
        this(username, SortCriteria.DATE, SortCriteria.DATE);
    }

    public String getUsername() {
        return username;
    }

    public SortCriteria getOpenSortCriteria() {
        return openSortCriteria;
    }

    public SortCriteria getHistoricalSortCriteria() {
        return historicalSortCriteria;
    }
}
