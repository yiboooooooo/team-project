package stakemate.use_case.view_market;

public interface ViewMarketOutputBoundary {
    void presentMatches(MatchesResponseModel responseModel);
    void presentMarketsForMatch(MarketsResponseModel responseModel);
    void presentOrderBook(OrderBookResponseModel responseModel);
    void presentError(String userMessage);
}
