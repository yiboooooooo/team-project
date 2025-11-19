package stakemate.interface_adapter.view_market;

import stakemate.use_case.view_market.MarketsResponseModel;
import stakemate.use_case.view_market.MatchSummary;
import stakemate.use_case.view_market.OrderBookResponseModel;

import java.util.List;

public interface MarketsView {
    void showMatches(List<MatchSummary> matches, String emptyStateMessage);

    void showMarketsForMatch(MarketsResponseModel responseModel);

    void showOrderBook(OrderBookResponseModel responseModel);

    void showError(String message);
}
