package stakemate.use_case.view_market.facade;

import stakemate.entity.Market;
import stakemate.entity.Match;
import stakemate.entity.OrderBook;
import stakemate.use_case.view_market.*;

import java.util.List;

/**
 * [Facade Pattern]
 * Hides the complexity of multiple repositories (Match, Market, OrderBook)
 * behind a single interface for the Interactor.
 */
public class MarketDataFacade {
    private final MatchRepository matchRepository;
    private final MarketRepository marketRepository;
    private final OrderBookGateway orderBookGateway;

    public MarketDataFacade(MatchRepository matchRepo,
                            MarketRepository marketRepo,
                            OrderBookGateway obGateway) {
        this.matchRepository = matchRepo;
        this.marketRepository = marketRepo;
        this.orderBookGateway = obGateway;
    }

    public List<Match> getAllMatches() throws RepositoryException {
        // Facade could also handle caching or complex fetching logic here
        return matchRepository.findAllMatches();
    }

    public List<Market> getMarketsForMatch(String matchId) throws RepositoryException {
        return marketRepository.findByMatchId(matchId);
    }

    public OrderBook getOrderBookSnapshot(String marketId) throws RepositoryException {
        return orderBookGateway.getSnapshot(marketId);
    }

    // Pass-through for subscription management
    public void subscribeToOrderBook(String marketId, OrderBookSubscriber subscriber) {
        orderBookGateway.subscribe(marketId, subscriber);
    }

    public void unsubscribeFromOrderBook(String marketId, OrderBookSubscriber subscriber) {
        orderBookGateway.unsubscribe(marketId, subscriber);
    }

    // API Refresh Trigger (Bridge to repo specific logic)
    public void refreshApi() throws RepositoryException {
        if (matchRepository instanceof stakemate.data_access.in_memory.InMemoryMatchRepository) {
            ((stakemate.data_access.in_memory.InMemoryMatchRepository) matchRepository).syncWithApiData();
        }
    }
}
