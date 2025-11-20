package stakemate.use_case.view_market;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import stakemate.entity.Market;
import stakemate.entity.MarketStatus;
import stakemate.entity.Match;
import stakemate.entity.MatchStatus;
import stakemate.entity.OrderBook;
import stakemate.entity.OrderBookEntry;
import stakemate.entity.Side;
import stakemate.use_case.view_market.facade.MarketDataFacade;
import stakemate.use_case.view_market.strategy.NameSortStrategy;

class ViewMarketInteractorTest {

    // Define dummy repositories to satisfy Facade constructor,
    // though we will override Facade methods in the Stub.
    private static final MatchRepository DUMMY_MATCH_REPO = () -> new ArrayList<>();
    private static final MarketRepository DUMMY_MARKET_REPO = matchId -> new ArrayList<>();
    private static final OrderBookGateway DUMMY_OB_GATEWAY = new OrderBookGateway() {
        @Override
        public OrderBook getSnapshot(final String marketId) {
            return null;
        }

        @Override
        public void subscribe(final String marketId, final OrderBookSubscriber subscriber) {
        }

        @Override
        public void unsubscribe(final String marketId, final OrderBookSubscriber subscriber) {
        }
    };
    private StubMarketDataFacade stubFacade;
    private ViewMarketInteractor interactor;

    @BeforeEach
    void setUp() {
        // Initialize the Stub Facade
        stubFacade = new StubMarketDataFacade();
    }

    // =========================================================================
    // Test: loadMatches()
    // =========================================================================

    @Test
    void testLoadMatches_Success() {
        // Arrange
        stubFacade.matchesToReturn.add(new Match("match1", "Home", "Away", MatchStatus.UPCOMING, LocalDateTime.now()));

        final ViewMarketOutputBoundary successPresenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel responseModel) {
                assertEquals(1, responseModel.getMatches().size());
                assertEquals("Home vs Away", responseModel.getMatches().get(0).getLabel());
                assertNull(responseModel.getEmptyStateMessage());
            }

            @Override
            public void presentMarketsForMatch(final MarketsResponseModel r) {
                fail("Unexpected call");
            }

            @Override
            public void presentOrderBook(final OrderBookResponseModel r) {
                fail("Unexpected call");
            }

            @Override
            public void presentError(final String m) {
                fail("Unexpected error: " + m);
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, successPresenter);
        interactor.loadMatches();
    }

    @Test
    void testLoadMatches_Empty() {
        // Arrange
        stubFacade.matchesToReturn.clear();

        final ViewMarketOutputBoundary emptyPresenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel responseModel) {
                assertTrue(responseModel.getMatches().isEmpty());
                assertEquals("No upcoming games", responseModel.getEmptyStateMessage());
            }

            @Override
            public void presentMarketsForMatch(final MarketsResponseModel r) {
                fail("Unexpected call");
            }

            @Override
            public void presentOrderBook(final OrderBookResponseModel r) {
                fail("Unexpected call");
            }

            @Override
            public void presentError(final String m) {
                fail("Unexpected error: " + m);
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, emptyPresenter);
        interactor.loadMatches();
    }

    @Test
    void testLoadMatches_Failure() {
        // Arrange
        stubFacade.shouldThrowOnMatches = true;

        final ViewMarketOutputBoundary errorPresenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) {
                fail("Unexpected success");
            }

            @Override
            public void presentMarketsForMatch(final MarketsResponseModel r) {
                fail("Unexpected call");
            }

            @Override
            public void presentOrderBook(final OrderBookResponseModel r) {
                fail("Unexpected call");
            }

            @Override
            public void presentError(final String userMessage) {
                assertTrue(userMessage.contains("problem loading matches"));
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, errorPresenter);
        interactor.loadMatches();
    }

    // =========================================================================
    // Test: refreshFromApi()
    // =========================================================================

    @Test
    void testRefreshFromApi_Success() {
        // Arrange
        final ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel responseModel) {
                // Should be called after refresh logic inside loadMatches
                assertNotNull(responseModel);
            }

            @Override
            public void presentMarketsForMatch(final MarketsResponseModel r) {
            }

            @Override
            public void presentOrderBook(final OrderBookResponseModel r) {
            }

            @Override
            public void presentError(final String m) {
                fail("Unexpected error");
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);
        interactor.refreshFromApi();
        assertTrue(stubFacade.refreshCalled);
    }

    @Test
    void testRefreshFromApi_Failure() {
        // Arrange
        stubFacade.shouldThrowOnRefresh = true;

        final ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) {
                fail("Unexpected success");
            }

            @Override
            public void presentMarketsForMatch(final MarketsResponseModel r) {
            }

            @Override
            public void presentOrderBook(final OrderBookResponseModel r) {
            }

            @Override
            public void presentError(final String userMessage) {
                assertTrue(userMessage.startsWith("Error refreshing from API"));
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);
        interactor.refreshFromApi();
    }

    // =========================================================================
    // Test: matchSelected() (includes Decorator, Builder, Strategy tests)
    // =========================================================================

    @Test
    void testMatchSelected_WithCachedMatch_AndMarkets() {
        // Arrange
        // 1. Populate cache via loadMatches first
        final Match match = new Match("m1", "Raptors", "Celtics", MatchStatus.UPCOMING, LocalDateTime.now());
        stubFacade.matchesToReturn.add(match);
        // Constructor: id, matchId, name, status
        final Market openMarket = new Market("mk1", "m1", "Moneyline", MarketStatus.OPEN);
        final Market closedMarket = new Market("mk2", "m1", "Total", MarketStatus.CLOSED);
        stubFacade.marketsToReturn.add(closedMarket);
        stubFacade.marketsToReturn.add(openMarket);

        final ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) {
            }

            @Override
            public void presentMarketsForMatch(final MarketsResponseModel responseModel) {
                assertEquals("m1", responseModel.getMatchId());
                assertEquals("Raptors vs Celtics", responseModel.getMatchTitle());

                final List<MarketSummary> markets = responseModel.getMarkets();
                assertEquals(2, markets.size());
                // mk1 is OPEN, mk2 is CLOSED. mk1 should be first.
                assertEquals("mk1", markets.get(0).getId());
                assertTrue(markets.get(0).getName().contains("Moneyline"));
                assertTrue(markets.get(0).toString().contains("HOT"), "Decorator should add HOT tag");
                assertFalse(markets.get(1).toString().contains("HOT"));
            }

            @Override
            public void presentOrderBook(final OrderBookResponseModel r) {
            }

            @Override
            public void presentError(final String m) {
                fail("Unexpected error");
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);
        interactor.loadMatches();

        // Act
        interactor.matchSelected("m1");
    }

    @Test
    void testMatchSelected_MatchNotInCache() {
        // Arrange
        // Not calling loadMatches, so cache is empty

        final ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) {
            }

            @Override
            public void presentMarketsForMatch(final MarketsResponseModel responseModel) {
                // Fallback title logic
                assertEquals("Match m99", responseModel.getMatchTitle());
                assertEquals("No markets available.", responseModel.getEmptyStateMessage());
            }

            @Override
            public void presentOrderBook(final OrderBookResponseModel r) {
            }

            @Override
            public void presentError(final String m) {
                fail("Unexpected error");
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);
        interactor.matchSelected("m99");
    }

    @Test
    void testMatchSelected_Failure() {
        // Arrange
        stubFacade.shouldThrowOnMarkets = true;

        final ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) {
            }

            @Override
            public void presentMarketsForMatch(final MarketsResponseModel r) {
                fail("Unexpected success");
            }

            @Override
            public void presentOrderBook(final OrderBookResponseModel r) {
            }

            @Override
            public void presentError(final String userMessage) {
                assertEquals("There was a problem loading markets.", userMessage);
            }
        };
        interactor = new ViewMarketInteractor(stubFacade, presenter);
        interactor.matchSelected("m1");
    }

    @Test
    void testSetStrategy() {
        // Arrange
        // Fix: Added matchId "m1" to constructors
        final Market openMarket = new Market("mk1", "m1", "Z-Name", MarketStatus.OPEN);
        final Market closedMarket = new Market("mk2", "m1", "A-Name", MarketStatus.CLOSED);
        stubFacade.marketsToReturn.add(openMarket);
        stubFacade.marketsToReturn.add(closedMarket);

        final ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) {
            }

            @Override
            public void presentMarketsForMatch(final MarketsResponseModel responseModel) {
                // With NameSortStrategy, "A-Name" (closed) should come before "Z-Name" (open)
                assertEquals("mk2", responseModel.getMarkets().get(0).getId());
            }

            @Override
            public void presentOrderBook(final OrderBookResponseModel r) {
            }

            @Override
            public void presentError(final String m) {
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);
        interactor.setMarketSortStrategy(new NameSortStrategy());
        interactor.matchSelected("m1");
    }

    // =========================================================================
    // Test: marketSelected() & Observer Logic
    // =========================================================================

    @Test
    void testMarketSelected_Success_And_Switching() {
        // Arrange
        // Use a mutable array to update the expected ID dynamically
        final String[] expectedId = {"mk1"};

        final OrderBook book1 = new OrderBook("mk1", new ArrayList<>(), new ArrayList<>());
        stubFacade.snapshotToReturn = book1;

        final ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) {
            }

            @Override
            public void presentMarketsForMatch(final MarketsResponseModel r) {
            }

            @Override
            public void presentOrderBook(final OrderBookResponseModel responseModel) {
                assertNotNull(responseModel.getOrderBook());
                assertEquals(expectedId[0], responseModel.getOrderBook().getMarketId());
            }

            @Override
            public void presentError(final String m) {
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);
        interactor.marketSelected("mk1");
        assertTrue(stubFacade.subscribedMarkets.contains("mk1"));
        // Reset spy list
        stubFacade.subscribedMarkets.clear();
        stubFacade.unsubscribedMarkets.clear();
        expectedId[0] = "mk2";
        stubFacade.snapshotToReturn = new OrderBook("mk2", new ArrayList<>(), new ArrayList<>());
        interactor.marketSelected("mk2");
        assertTrue(stubFacade.unsubscribedMarkets.contains("mk1"));
        assertTrue(stubFacade.subscribedMarkets.contains("mk2"));
    }

    @Test
    void testMarketSelected_Failure() {
        // Arrange
        stubFacade.shouldThrowOnSnapshot = true;

        final ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) {
            }

            @Override
            public void presentMarketsForMatch(final MarketsResponseModel r) {
            }

            @Override
            public void presentOrderBook(final OrderBookResponseModel responseModel) {
                assertTrue(responseModel.isReconnecting());
                assertEquals("Reconnecting...", responseModel.getMessage());
            }

            @Override
            public void presentError(final String m) {
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);
        interactor.marketSelected("mk1");
    }

    @Test
    void testObserverMethods() {
        // Test onOrderBookUpdated
        final ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) {
            }

            @Override
            public void presentMarketsForMatch(final MarketsResponseModel r) {
            }

            @Override
            public void presentOrderBook(final OrderBookResponseModel responseModel) {
                if (responseModel.isReconnecting()) {
                    return;
                }
                assertFalse(responseModel.isEmpty());
            }

            @Override
            public void presentError(final String m) {
            }
        };
        interactor = new ViewMarketInteractor(stubFacade, presenter);

        final List<OrderBookEntry> bids = List.of(new OrderBookEntry(Side.BUY, 1.5, 10));
        final OrderBook update = new OrderBook("mk1", bids, new ArrayList<>());
        interactor.onOrderBookUpdated(update);
        final ViewMarketOutputBoundary errorPresenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) {
            }

            @Override
            public void presentMarketsForMatch(final MarketsResponseModel r) {
            }

            @Override
            public void presentOrderBook(final OrderBookResponseModel responseModel) {
                assertTrue(responseModel.isReconnecting());
                assertEquals("Lost connection", responseModel.getMessage());
            }

            @Override
            public void presentError(final String m) {
            }
        };
        interactor = new ViewMarketInteractor(stubFacade, errorPresenter);
        interactor.onConnectionError("Lost connection");
        final ViewMarketOutputBoundary restorePresenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) {
            }

            @Override
            public void presentMarketsForMatch(final MarketsResponseModel r) {
            }

            @Override
            public void presentOrderBook(final OrderBookResponseModel responseModel) {
                assertFalse(responseModel.isReconnecting());
                assertEquals("Connection restored", responseModel.getMessage());
            }

            @Override
            public void presentError(final String m) {
            }
        };
        interactor = new ViewMarketInteractor(stubFacade, restorePresenter);
        interactor.onConnectionRestored();
    }

    // =========================================================================
    // Internal Stub Class to Mock MarketDataFacade
    // =========================================================================

    private static class StubMarketDataFacade extends MarketDataFacade {
        boolean shouldThrowOnMatches = false;
        boolean shouldThrowOnRefresh = false;
        boolean shouldThrowOnMarkets = false;
        boolean shouldThrowOnSnapshot = false;

        boolean refreshCalled = false;

        List<Match> matchesToReturn = new ArrayList<>();
        List<Market> marketsToReturn = new ArrayList<>();
        OrderBook snapshotToReturn = null;

        List<String> subscribedMarkets = new ArrayList<>();
        List<String> unsubscribedMarkets = new ArrayList<>();

        public StubMarketDataFacade() {
            super(DUMMY_MATCH_REPO, DUMMY_MARKET_REPO, DUMMY_OB_GATEWAY);
        }

        @Override
        public List<Match> getAllMatches() throws RepositoryException {
            if (shouldThrowOnMatches) {
                throw new RepositoryException("DB Error");
            }
            return matchesToReturn;
        }

        @Override
        public void refreshApi() throws RepositoryException {
            if (shouldThrowOnRefresh) {
                throw new RepositoryException("API Error");
            }
            refreshCalled = true;
        }

        @Override
        public List<Market> getMarketsForMatch(final String matchId) throws RepositoryException {
            if (shouldThrowOnMarkets) {
                throw new RepositoryException("DB Error");
            }
            return marketsToReturn;
        }

        @Override
        public OrderBook getOrderBookSnapshot(final String marketId) throws RepositoryException {
            if (shouldThrowOnSnapshot) {
                throw new RepositoryException("DB Error");
            }
            if (snapshotToReturn == null) {
                return new OrderBook(marketId, new ArrayList<>(), new ArrayList<>());
            }
            return snapshotToReturn;
        }

        @Override
        public void subscribeToOrderBook(final String marketId, final OrderBookSubscriber subscriber) {
            subscribedMarkets.add(marketId);
        }

        @Override
        public void unsubscribeFromOrderBook(final String marketId, final OrderBookSubscriber subscriber) {
            unsubscribedMarkets.add(marketId);
        }
    }
}
