package stakemate.use_case.view_market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stakemate.entity.Market;
import stakemate.entity.MarketStatus;
import stakemate.entity.Match;
import stakemate.entity.OrderBook;
import stakemate.use_case.view_market.builder.MarketsResponseModelBuilder;
import stakemate.use_case.view_market.decorator.HotAbstractMarketDecorator;
import stakemate.use_case.view_market.facade.MarketDataFacade;
import stakemate.use_case.view_market.strategy.MarketSortStrategy;
import stakemate.use_case.view_market.strategy.StatusSortStrategy;

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
    private MarketSortStrategy marketSortStrategy;
    private String currentSubscribedMarketId;

    public ViewMarketInteractor(final MarketDataFacade dataFacade,
                                final ViewMarketOutputBoundary presenter) {
        this.dataFacade = dataFacade;
        this.presenter = presenter;
        this.marketSortStrategy = new StatusSortStrategy();
    }

    // Setter to change strategy at runtime
    public void setMarketSortStrategy(final MarketSortStrategy strategy) {
        this.marketSortStrategy = strategy;
    }

    @Override
    public void loadMatches() {
        try {
            final List<Match> matches = dataFacade.getAllMatches();
            matchesById.clear();
            final List<MatchSummary> summaries = new ArrayList<>();

            for (final Match m : matches) {
                matchesById.put(m.getId(), m);
                final String label = m.getHomeTeam() + " vs " + m.getAwayTeam();
                final String statusLabel = m.getStatus().name();
                summaries.add(new MatchSummary(m.getId(), label, statusLabel));
            }

            final String emptyMessage;
            if (summaries.isEmpty()) {
                emptyMessage = "No upcoming games";
            }
            else {
                emptyMessage = null;
            }
            presenter.presentMatches(new MatchesResponseModel(summaries, emptyMessage));
        }
        catch (final RepositoryException ex) {
            presenter.presentError("There was a problem loading matches. Please try again.");
        }
    }

    @Override
    public void refreshFromApi() {
        try {
            dataFacade.refreshApi();
            loadMatches();
        }
        catch (final RepositoryException ex) {
            presenter.presentError("Error refreshing from API: " + ex.getMessage());
        }
    }

    @Override
    public void matchSelected(final String matchId) {
        // Unsubscribe from the old market to stop ghost updates
        if (currentSubscribedMarketId != null) {
            dataFacade.unsubscribeFromOrderBook(currentSubscribedMarketId, this);
            currentSubscribedMarketId = null;
        }

        final Match match = matchesById.get(matchId);
        final String title;
        if (match != null) {
            title = match.getHomeTeam() + " vs " + match.getAwayTeam();
        }
        else {
            title = "Match " + matchId;
        }

        try {
            final List<Market> markets = dataFacade.getMarketsForMatch(matchId);
            final List<MarketSummary> summaries = new ArrayList<>();

            for (final Market market : markets) {
                final boolean open = market.getStatus() == MarketStatus.OPEN;
                final String statusLabel;
                if (open) {
                    statusLabel = "Open";
                }
                else {
                    statusLabel = "Closed";
                }

                MarketSummary summary = new MarketSummary(
                    market.getId(),
                    market.getName(),
                    statusLabel,
                    open
                );
                if (open) {
                    summary = new HotAbstractMarketDecorator(summary);
                }

                summaries.add(summary);
            }

            // [Strategy Pattern]: Sort the list
            marketSortStrategy.sort(summaries);
            final MarketsResponseModel response = new MarketsResponseModelBuilder()
                .setMatchId(matchId)
                .setMatchTitle(title)
                .setMarkets(summaries)
                .build();

            presenter.presentMarketsForMatch(response);

        }
        catch (final RepositoryException ex) {
            presenter.presentError("There was a problem loading markets.");
        }
    }

    @Override
    public void marketSelected(final String marketId) {
        if (currentSubscribedMarketId != null && !currentSubscribedMarketId.equals(marketId)) {
            dataFacade.unsubscribeFromOrderBook(currentSubscribedMarketId, this);
        }
        currentSubscribedMarketId = marketId;

        try {
            final OrderBook orderBook = dataFacade.getOrderBookSnapshot(marketId);
            final boolean empty = orderBook.getBids().isEmpty() && orderBook.getAsks().isEmpty();
            final String msg;
            if (empty) {
                msg = "No orders yet";
            }
            else {
                msg = null;
            }

            presenter.presentOrderBook(
                new OrderBookResponseModel(orderBook, empty, false, msg)
            );

            dataFacade.subscribeToOrderBook(marketId, this);

        }
        catch (final RepositoryException ex) {
            presenter.presentOrderBook(
                new OrderBookResponseModel(null, false, true, "Reconnecting...")
            );
        }
    }

    // [Observer Pattern]: Callback methods
    @Override
    public void onOrderBookUpdated(final OrderBook orderBook) {
        final boolean empty = orderBook.getBids().isEmpty() && orderBook.getAsks().isEmpty();
        final String msg;
        if (empty) {
            msg = "No orders yet";
        }
        else {
            msg = null;
        }
        presenter.presentOrderBook(
            new OrderBookResponseModel(orderBook, empty, false, msg)
        );
    }

    @Override
    public void onConnectionError(final String message) {
        final String displayMsg;
        if (message != null) {
            displayMsg = message;
        }
        else {
            displayMsg = "Reconnecting...";
        }
        presenter.presentOrderBook(
            new OrderBookResponseModel(null, false, true, displayMsg)
        );
    }

    @Override
    public void onConnectionRestored() {
        presenter.presentOrderBook(
            new OrderBookResponseModel(null, false, false, "Connection restored")
        );
    }
}
