package stakemate.use_case.view_market;

public interface ViewMarketInputBoundary {
    /**
     * Loads the list of available matches from the repository.
     */
    void loadMatches();

    /**
     * Triggers a refresh of game data from the external API and updates the view.
     */
    void refreshFromApi();

    /**
     * Handles the event when a match is selected by the user.
     * Loads the markets associated with that match.
     *
     * @param matchId the ID of the selected match.
     */
    void matchSelected(String matchId);

    /**
     * Handles the event when a market is selected by the user.
     * Loads the order book for that market.
     *
     * @param marketId the ID of the selected market.
     */
    void marketSelected(String marketId);
}
