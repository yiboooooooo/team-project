package stakemate.use_case.view_market;

import stakemate.entity.Market;
import stakemate.entity.MarketStatus;
import stakemate.entity.Match;
import stakemate.entity.OrderBook;
import stakemate.use_case.view_market.builder.MarketsResponseModelBuilder;
import stakemate.use_case.view_market.decorator.HotMarketDecorator;
import stakemate.use_case.view_market.facade.MarketDataFacade;
import stakemate.use_case.view_market.strategy.MarketSortStrategy;
import stakemate.use_case.view_market.strategy.StatusSortStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * [Observer Pattern]: Implements OrderBookSubscriber to observe data changes.
 * Uses [Facade], [Strategy], [Builder], and [Decorator] internally.
 */
public class ViewMarketInteractor implements
    ViewMarketInputBoundary,
    OrderBookSubscriber {

    // [Facade Pattern]: Replaces individual Repos
    private final MarketDataFacade dataFacade;
    private final ViewMarketOutputBoundary presenter;
    private final Map<String, Match> matchesById = new HashMap<>();
    // [Strategy Pattern]: Strategy for sorting markets
    private MarketSortStrategy marketSortStrategy;
    private String currentSubscribedMarketId;

    public ViewMarketInteractor(MarketDataFacade dataFacade,
                                ViewMarketOutputBoundary presenter) {
        this.dataFacade = dataFacade;
        this.presenter = presenter;
        // Default strategy
        this.marketSortStrategy = new StatusSortStrategy();
    }

    // Setter to change strategy at runtime
    public void setMarketSortStrategy(MarketSortStrategy strategy) {
        this.marketSortStrategy = strategy;
    }

    @Override
    public void loadMatches() {
        try {
            List<Match> matches = dataFacade.getAllMatches();
            matchesById.clear();
            List<MatchSummary> summaries = new ArrayList<>();

            for (Match m : matches) {
                matchesById.put(m.getId(), m);
                String label = m.getHomeTeam() + " vs " + m.getAwayTeam();
                String statusLabel = m.getStatus().name();
                summaries.add(new MatchSummary(m.getId(), label, statusLabel));
            }

            String emptyMessage = summaries.isEmpty() ? "No upcoming games" : null;
            presenter.presentMatches(new MatchesResponseModel(summaries, emptyMessage));
        } catch (RepositoryException e) {
            presenter.presentError("There was a problem loading matches. Please try again.");
        }
    }

    @Override
    public void refreshFromApi() {
        try {
            dataFacade.refreshApi();
            loadMatches();
        } catch (RepositoryException e) {
            presenter.presentError("Error refreshing from API: " + e.getMessage());
        }
    }

    @Override
    public void matchSelected(String matchId) {
        Match match = matchesById.get(matchId);
        String title = match != null
            ? match.getHomeTeam() + " vs " + match.getAwayTeam()
            : "Match " + matchId;

        try {
            List<Market> markets = dataFacade.getMarketsForMatch(matchId);
            List<MarketSummary> summaries = new ArrayList<>();

            for (Market market : markets) {
                boolean open = market.getStatus() == MarketStatus.OPEN;
                String statusLabel = open ? "Open" : "Closed";

                MarketSummary summary = new MarketSummary(
                    market.getId(),
                    market.getName(),
                    statusLabel,
                    open
                );

                // [Decorator Pattern]: Mark open markets as "HOT"
                if (open) {
                    summary = new HotMarketDecorator(summary);
                }

                summaries.add(summary);
            }

            // [Strategy Pattern]: Sort the list
            marketSortStrategy.sort(summaries);

            // [Builder Pattern]: Construct response
            MarketsResponseModel response = new MarketsResponseModelBuilder()
                .setMatchId(matchId)
                .setMatchTitle(title)
                .setMarkets(summaries)
                .build();

            presenter.presentMarketsForMatch(response);

        } catch (RepositoryException e) {
            presenter.presentError("There was a problem loading markets.");
        }
    }

    @Override
    public void marketSelected(String marketId) {
        if (currentSubscribedMarketId != null && !currentSubscribedMarketId.equals(marketId)) {
            dataFacade.unsubscribeFromOrderBook(currentSubscribedMarketId, this);
        }
        currentSubscribedMarketId = marketId;

        try {
            OrderBook orderBook = dataFacade.getOrderBookSnapshot(marketId);
            boolean empty = orderBook.getBids().isEmpty() && orderBook.getAsks().isEmpty();
            String msg = empty ? "No orders yet" : null;

            presenter.presentOrderBook(
                new OrderBookResponseModel(orderBook, empty, false, msg)
            );

            dataFacade.subscribeToOrderBook(marketId, this);

        } catch (RepositoryException e) {
            presenter.presentOrderBook(
                new OrderBookResponseModel(null, false, true, "Reconnecting...")
            );
        }
    }

    // [Observer Pattern]: Callback methods
    @Override
    public void onOrderBookUpdated(OrderBook orderBook) {
        boolean empty = orderBook.getBids().isEmpty() && orderBook.getAsks().isEmpty();
        String msg = empty ? "No orders yet" : null;
        presenter.presentOrderBook(
            new OrderBookResponseModel(orderBook, empty, false, msg)
        );
    }

    @Override
    public void onConnectionError(String message) {
        presenter.presentOrderBook(
            new OrderBookResponseModel(null, false, true,
                message != null ? message : "Reconnecting...")
        );
    }

    @Override
    public void onConnectionRestored() {
        presenter.presentOrderBook(
            new OrderBookResponseModel(null, false, false, "Connection restored")
        );
    }
}
