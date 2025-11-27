package stakemate.use_case.view_market;

public interface ViewMarketOutputBoundary {
    /**
     * Presents the list of matches to the view.
     *
     * @param responseModel the data containing matches to display.
     */
    void presentMatches(MatchesResponseModel responseModel);

    /**
     * Presents the markets for a specific match to the view.
     *
     * @param responseModel the data containing markets for the selected match.
     */
    void presentMarketsForMatch(MarketsResponseModel responseModel);

    /**
     * Presents the order book data to the view.
     *
     * @param responseModel the data containing the order book snapshot.
     */
    void presentOrderBook(OrderBookResponseModel responseModel);

    /**
     * Presents an error message to the user.
     *
     * @param userMessage the error message to display.
     */
    void presentError(String userMessage);
}
