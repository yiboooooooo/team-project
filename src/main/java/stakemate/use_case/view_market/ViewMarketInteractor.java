package stakemate.use_case.view_market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stakemate.entity.Match;
import stakemate.entity.Market;
import stakemate.entity.MarketStatus;
import stakemate.entity.OrderBook;

public class ViewMarketInteractor implements
        ViewMarketInputBoundary,
        OrderBookSubscriber {

    private final MatchRepository matchRepository;
    private final MarketRepository marketRepository;
    private final OrderBookGateway orderBookGateway;
    private final ViewMarketOutputBoundary presenter;

    private final Map<String, Match> matchesById = new HashMap<>();
    private String currentSubscribedMarketId;

    public ViewMarketInteractor(MatchRepository matchRepository,
                                MarketRepository marketRepository,
                                OrderBookGateway orderBookGateway,
                                ViewMarketOutputBoundary presenter) {
        this.matchRepository = matchRepository;
        this.marketRepository = marketRepository;
        this.orderBookGateway = orderBookGateway;
        this.presenter = presenter;
    }

    @Override
    public void loadMatches() {
        try {
            List<Match> matches = matchRepository.findAllMatches();
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
    public void matchSelected(String matchId) {
        Match match = matchesById.get(matchId);
        String title = match != null
                ? match.getHomeTeam() + " vs " + match.getAwayTeam()
                : "Match " + matchId;

        try {
            List<Market> markets = marketRepository.findByMatchId(matchId);
            List<MarketSummary> summaries = new ArrayList<>();

            for (Market market : markets) {
                boolean open = market.getStatus() == MarketStatus.OPEN;
                String statusLabel = open ? "Open" : "Closed";
                summaries.add(new MarketSummary(
                        market.getId(),
                        market.getName(),
                        statusLabel,
                        open
                ));
            }

            String emptyMessage = summaries.isEmpty()
                    ? "No markets available for this match yet."
                    : null;

            presenter.presentMarketsForMatch(new MarketsResponseModel(
                    matchId, title, summaries, emptyMessage
            ));
        } catch (RepositoryException e) {
            presenter.presentError("There was a problem loading markets. Please try again.");
        }
    }

    @Override
    public void marketSelected(String marketId) {
        if (currentSubscribedMarketId != null && !currentSubscribedMarketId.equals(marketId)) {
            orderBookGateway.unsubscribe(currentSubscribedMarketId, this);
        }
        currentSubscribedMarketId = marketId;

        try {
            OrderBook orderBook = orderBookGateway.getSnapshot(marketId);
            boolean empty = orderBook.getBids().isEmpty() && orderBook.getAsks().isEmpty();
            String msg = empty ? "No orders yet" : null;

            presenter.presentOrderBook(
                    new OrderBookResponseModel(orderBook, empty, false, msg)
            );

            orderBookGateway.subscribe(marketId, this);

        } catch (RepositoryException e) {
            presenter.presentOrderBook(
                    new OrderBookResponseModel(null, false, true, "Reconnecting...")
            );
        }
    }

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
