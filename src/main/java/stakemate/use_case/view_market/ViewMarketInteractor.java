package stakemate.use_case.view_market;

import stakemate.entity.Market;
import stakemate.entity.Match;
import stakemate.entity.OrderBook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Refactored with Strategy and Observer patterns
 */
public class ViewMarketInteractor implements
        ViewMarketInputBoundary,
        OrderBookSubscriber {

    private final MatchRepository matchRepository;
    private final MarketRepository marketRepository;
    private final OrderBookGateway orderBookGateway;
    private final ViewMarketOutputBoundary presenter;
    // Observer pattern for market updates
    private final List<MarketUpdateObserver> observers = new ArrayList<>();
    // Facade for complex operations
    private final MarketOperationsFacade marketFacade;
    // Factory for response models
    private final ResponseModelFactory responseFactory;
    private final Map<String, Match> matchesById = new HashMap<>();
    // Strategy pattern for market display
    private MarketDisplayStrategy displayStrategy;
    private String currentSubscribedMarketId;

    public ViewMarketInteractor(MatchRepository matchRepository,
                                MarketRepository marketRepository,
                                OrderBookGateway orderBookGateway,
                                ViewMarketOutputBoundary presenter) {
        this.matchRepository = matchRepository;
        this.marketRepository = marketRepository;
        this.orderBookGateway = orderBookGateway;
        this.presenter = presenter;
        this.displayStrategy = new DefaultMarketDisplayStrategy();
        this.marketFacade = new MarketOperationsFacade(marketRepository, orderBookGateway);
        this.responseFactory = new ResponseModelFactory();
    }

    public void setDisplayStrategy(MarketDisplayStrategy strategy) {
        this.displayStrategy = strategy;
    }

    public void addObserver(MarketUpdateObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers(MarketUpdateEvent event) {
        for (MarketUpdateObserver observer : observers) {
            observer.onMarketUpdate(event);
        }
    }

    @Override
    public void loadMatches() {
        ViewMarketCommand command = new LoadMatchesCommand(this::executeLoadMatches);
        command.execute();
    }

    private void executeLoadMatches() {
        try {
            List<Match> matches = matchRepository.findAllMatches();
            matchesById.clear();

            List<MatchSummary> summaries = displayStrategy.formatMatches(matches, matchesById);

            String emptyMessage = summaries.isEmpty() ? "No upcoming games" : null;
            MatchesResponseModel response = responseFactory.createMatchesResponse(summaries, emptyMessage);

            presenter.presentMatches(response);
            notifyObservers(new MarketUpdateEvent(MarketUpdateEvent.Type.MATCHES_LOADED, matches.size()));
        } catch (RepositoryException e) {
            presenter.presentError("There was a problem loading matches. Please try again.");
        }
    }

    @Override
    public void refreshFromApi() {
        ViewMarketCommand command = new RefreshFromApiCommand(this::executeRefreshFromApi);
        command.execute();
    }

    private void executeRefreshFromApi() {
        try {
            if (matchRepository instanceof stakemate.data_access.in_memory.InMemoryMatchRepository) {
                ((stakemate.data_access.in_memory.InMemoryMatchRepository) matchRepository).syncWithApiData();
            }
            loadMatches();
        } catch (RepositoryException e) {
            presenter.presentError("Error refreshing from API: " + e.getMessage());
        }
    }

    @Override
    public void matchSelected(String matchId) {
        ViewMarketCommand command = new SelectMatchCommand(matchId, this::executeMatchSelected);
        command.execute();
    }

    private void executeMatchSelected(String matchId) {
        Match match = matchesById.get(matchId);
        String title = match != null
                ? match.getHomeTeam() + " vs " + match.getAwayTeam()
                : "Match " + matchId;

        try {
            List<Market> markets = marketFacade.getMarketsForMatch(matchId);
            List<MarketSummary> summaries = displayStrategy.formatMarkets(markets);

            String emptyMessage = summaries.isEmpty()
                    ? "No markets available for this match yet."
                    : null;

            MarketsResponseModel response = responseFactory.createMarketsResponse(
                    matchId, title, summaries, emptyMessage
            );

            presenter.presentMarketsForMatch(response);
            notifyObservers(new MarketUpdateEvent(MarketUpdateEvent.Type.MATCH_SELECTED, matchId));
        } catch (RepositoryException e) {
            presenter.presentError("There was a problem loading markets. Please try again.");
        }
    }

    @Override
    public void marketSelected(String marketId) {
        ViewMarketCommand command = new SelectMarketCommand(marketId, this::executeMarketSelected);
        command.execute();
    }

    private void executeMarketSelected(String marketId) {
        if (currentSubscribedMarketId != null && !currentSubscribedMarketId.equals(marketId)) {
            orderBookGateway.unsubscribe(currentSubscribedMarketId, this);
        }
        currentSubscribedMarketId = marketId;

        try {
            OrderBook orderBook = marketFacade.getOrderBook(marketId);
            OrderBookAdapter adapter = new StandardOrderBookAdapter();
            OrderBookResponseModel response = adapter.adapt(orderBook);

            presenter.presentOrderBook(response);
            orderBookGateway.subscribe(marketId, this);
            notifyObservers(new MarketUpdateEvent(MarketUpdateEvent.Type.MARKET_SELECTED, marketId));

        } catch (RepositoryException e) {
            presenter.presentOrderBook(
                    responseFactory.createReconnectingResponse()
            );
        }
    }

    @Override
    public void onOrderBookUpdated(OrderBook orderBook) {
        OrderBookAdapter adapter = new StandardOrderBookAdapter();
        OrderBookResponseModel response = adapter.adapt(orderBook);
        presenter.presentOrderBook(response);
        notifyObservers(new MarketUpdateEvent(MarketUpdateEvent.Type.ORDER_BOOK_UPDATED, orderBook));
    }

    @Override
    public void onConnectionError(String message) {
        presenter.presentOrderBook(
                responseFactory.createErrorResponse(message)
        );
    }

    @Override
    public void onConnectionRestored() {
        presenter.presentOrderBook(
                responseFactory.createRestoredResponse()
        );
    }
}