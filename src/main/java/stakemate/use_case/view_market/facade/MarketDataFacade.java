package stakemate.use_case.view_market.facade;

import java.util.List;

import stakemate.entity.Market;
import stakemate.entity.Match;
import stakemate.entity.OrderBook;
import stakemate.use_case.view_market.MarketRepository;
import stakemate.use_case.view_market.MatchRepository;
import stakemate.use_case.view_market.OrderBookGateway;
import stakemate.use_case.view_market.OrderBookSubscriber;
import stakemate.use_case.view_market.RepositoryException;

/**
 * [Facade Pattern]
 * Hides the complexity of multiple repositories (Match, Market, OrderBook)
 * behind a single interface for the Interactor.
 */
public class MarketDataFacade {
    private final MatchRepository matchRepository;
    private final MarketRepository marketRepository;
    private final OrderBookGateway orderBookGateway;

    public MarketDataFacade(final MatchRepository matchRepo,
                            final MarketRepository marketRepo,
                            final OrderBookGateway obGateway) {
        this.matchRepository = matchRepo;
        this.marketRepository = marketRepo;
        this.orderBookGateway = obGateway;
    }

    public List<Match> getAllMatches() throws RepositoryException {
        // Facade could also handle caching or complex fetching logic here
        return matchRepository.findAllMatches();
    }

    public List<Market> getMarketsForMatch(final String matchId) throws RepositoryException {
        return marketRepository.findByMatchId(matchId);
    }

    public OrderBook getOrderBookSnapshot(final String marketId) throws RepositoryException {
        return orderBookGateway.getSnapshot(marketId);
    }

    // Pass-through for subscription management
    public void subscribeToOrderBook(final String marketId, final OrderBookSubscriber subscriber) {
        orderBookGateway.subscribe(marketId, subscriber);
    }

    public void unsubscribeFromOrderBook(final String marketId, final OrderBookSubscriber subscriber) {
        orderBookGateway.unsubscribe(marketId, subscriber);
    }

    // API Refresh Trigger (Bridge to repo specific logic)
    public void refreshApi() throws RepositoryException {
        if (matchRepository instanceof stakemate.data_access.in_memory.InMemoryMatchRepository) {
            ((stakemate.data_access.in_memory.InMemoryMatchRepository) matchRepository).syncWithApiData();
        }
    }
}
