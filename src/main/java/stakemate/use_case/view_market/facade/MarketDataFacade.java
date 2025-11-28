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

    /**
     * Retrieves all available matches from the underlying repository.
     *
     * @return a list of matches.
     * @throws RepositoryException if data retrieval fails.
     */
    public List<Match> getAllMatches() throws RepositoryException {
        // Facade could also handle caching or complex fetching logic here
        return matchRepository.findAllMatches();
    }

    /**
     * Retrieves all markets associated with a specific match ID.
     *
     * @param matchId the ID of the match.
     * @return a list of markets for the given match.
     * @throws RepositoryException if data retrieval fails.
     */
    public List<Market> getMarketsForMatch(final String matchId) throws RepositoryException {
        return marketRepository.findByMatchId(matchId);
    }

    /**
     * Gets a snapshot of the current order book for a specific market.
     *
     * @param marketId the ID of the market.
     * @return the current order book.
     * @throws RepositoryException if data retrieval fails.
     */
    public OrderBook getOrderBookSnapshot(final String marketId) throws RepositoryException {
        return orderBookGateway.getSnapshot(marketId);
    }

    /**
     * Subscribes an observer to real-time updates for a specific order book.
     *
     * @param marketId   the ID of the market to watch.
     * @param subscriber the observer to notify.
     */
    public void subscribeToOrderBook(final String marketId, final OrderBookSubscriber subscriber) {
        orderBookGateway.subscribe(marketId, subscriber);
    }

    /**
     * Unsubscribes an observer from updates for a specific market.
     *
     * @param marketId   the ID of the market.
     * @param subscriber the observer to remove.
     */
    public void unsubscribeFromOrderBook(final String marketId, final OrderBookSubscriber subscriber) {
        orderBookGateway.unsubscribe(marketId, subscriber);
    }

    /**
     * Triggers a refresh of data from the external API if the repository supports it.
     *
     * @throws RepositoryException if the refresh operation fails.
     */
    public void refreshApi() throws RepositoryException {
        if (matchRepository instanceof stakemate.data_access.in_memory.InMemoryMatchRepository) {
            ((stakemate.data_access.in_memory.InMemoryMatchRepository) matchRepository).syncWithApiData();
        }
    }
}
