package stakemate.use_case.view_market;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stakemate.entity.*;
import stakemate.use_case.view_market.facade.MarketDataFacade;
import stakemate.use_case.view_market.strategy.NameSortStrategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ViewMarketInteractorTest {

    // Define dummy repositories to satisfy Facade constructor,
    // though we will override Facade methods in the Stub.
    private static final MatchRepository DUMMY_MATCH_REPO = () -> new ArrayList<>();
    private static final MarketRepository DUMMY_MARKET_REPO = matchId -> new ArrayList<>();
    private static final OrderBookGateway DUMMY_OB_GATEWAY = new OrderBookGateway() {
        @Override
        public OrderBook getSnapshot(String marketId) {
            return null;
        }

        @Override
        public void subscribe(String marketId, OrderBookSubscriber subscriber) {
        }

        @Override
        public void unsubscribe(String marketId, OrderBookSubscriber subscriber) {
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

        ViewMarketOutputBoundary successPresenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(MatchesResponseModel responseModel) {
                assertEquals(1, responseModel.getMatches().size());
                assertEquals("Home vs Away", responseModel.getMatches().get(0).getLabel());
                assertNull(responseModel.getEmptyStateMessage());
            }

            @Override
            public void presentMarketsForMatch(MarketsResponseModel r) {
                fail("Unexpected call");
            }

            @Override
            public void presentOrderBook(OrderBookResponseModel r) {
                fail("Unexpected call");
            }

            @Override
            public void presentError(String m) {
                fail("Unexpected error: " + m);
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, successPresenter);

        // Act
        interactor.loadMatches();
    }

    @Test
    void testLoadMatches_Empty() {
        // Arrange
        stubFacade.matchesToReturn.clear(); // Empty list

        ViewMarketOutputBoundary emptyPresenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(MatchesResponseModel responseModel) {
                assertTrue(responseModel.getMatches().isEmpty());
                assertEquals("No upcoming games", responseModel.getEmptyStateMessage());
            }

            @Override
            public void presentMarketsForMatch(MarketsResponseModel r) {
                fail("Unexpected call");
            }

            @Override
            public void presentOrderBook(OrderBookResponseModel r) {
                fail("Unexpected call");
            }

            @Override
            public void presentError(String m) {
                fail("Unexpected error: " + m);
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, emptyPresenter);

        // Act
        interactor.loadMatches();
    }

    @Test
    void testLoadMatches_Failure() {
        // Arrange
        stubFacade.shouldThrowOnMatches = true;

        ViewMarketOutputBoundary errorPresenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(MatchesResponseModel r) {
                fail("Unexpected success");
            }

            @Override
            public void presentMarketsForMatch(MarketsResponseModel r) {
                fail("Unexpected call");
            }

            @Override
            public void presentOrderBook(OrderBookResponseModel r) {
                fail("Unexpected call");
            }

            @Override
            public void presentError(String userMessage) {
                assertTrue(userMessage.contains("problem loading matches"));
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, errorPresenter);

        // Act
        interactor.loadMatches();
    }

    // =========================================================================
    // Test: refreshFromApi()
    // =========================================================================

    @Test
    void testRefreshFromApi_Success() {
        // Arrange
        ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(MatchesResponseModel responseModel) {
                // Should be called after refresh logic inside loadMatches
                assertNotNull(responseModel);
            }

            @Override
            public void presentMarketsForMatch(MarketsResponseModel r) {
            }

            @Override
            public void presentOrderBook(OrderBookResponseModel r) {
            }

            @Override
            public void presentError(String m) {
                fail("Unexpected error");
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);

        // Act
        interactor.refreshFromApi();

        // Assert
        assertTrue(stubFacade.refreshCalled);
    }

    @Test
    void testRefreshFromApi_Failure() {
        // Arrange
        stubFacade.shouldThrowOnRefresh = true;

        ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(MatchesResponseModel r) {
                fail("Unexpected success");
            }

            @Override
            public void presentMarketsForMatch(MarketsResponseModel r) {
            }

            @Override
            public void presentOrderBook(OrderBookResponseModel r) {
            }

            @Override
            public void presentError(String userMessage) {
                assertTrue(userMessage.startsWith("Error refreshing from API"));
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);

        // Act
        interactor.refreshFromApi();
    }

    // =========================================================================
    // Test: matchSelected() (includes Decorator, Builder, Strategy tests)
    // =========================================================================

    @Test
    void testMatchSelected_WithCachedMatch_AndMarkets() {
        // Arrange
        // 1. Populate cache via loadMatches first
        Match match = new Match("m1", "Raptors", "Celtics", MatchStatus.UPCOMING, LocalDateTime.now());
        stubFacade.matchesToReturn.add(match);

        // 2. Setup Markets (One OPEN, One CLOSED) to test Decorator and Sorting
        // Constructor: id, matchId, name, status
        Market openMarket = new Market("mk1", "m1", "Moneyline", MarketStatus.OPEN);
        Market closedMarket = new Market("mk2", "m1", "Total", MarketStatus.CLOSED);
        stubFacade.marketsToReturn.add(closedMarket); // Add closed first to test sorting
        stubFacade.marketsToReturn.add(openMarket);

        ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(MatchesResponseModel r) {
            }

            @Override
            public void presentMarketsForMatch(MarketsResponseModel responseModel) {
                assertEquals("m1", responseModel.getMatchId());
                assertEquals("Raptors vs Celtics", responseModel.getMatchTitle());

                List<MarketSummary> markets = responseModel.getMarkets();
                assertEquals(2, markets.size());

                // Test Strategy: Default is StatusSortStrategy (Open first)
                // mk1 is OPEN, mk2 is CLOSED. mk1 should be first.
                assertEquals("mk1", markets.get(0).getId());

                // Test Decorator: Open market should have "HOT" tag in name
                assertTrue(markets.get(0).getName().contains("Moneyline"));
                assertTrue(markets.get(0).toString().contains("HOT"), "Decorator should add HOT tag");

                // Closed market should not be decorated
                assertFalse(markets.get(1).toString().contains("HOT"));
            }

            @Override
            public void presentOrderBook(OrderBookResponseModel r) {
            }

            @Override
            public void presentError(String m) {
                fail("Unexpected error");
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);
        interactor.loadMatches(); // Pre-fill matchesById

        // Act
        interactor.matchSelected("m1");
    }

    @Test
    void testMatchSelected_MatchNotInCache() {
        // Arrange
        // Not calling loadMatches, so cache is empty

        ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(MatchesResponseModel r) {
            }

            @Override
            public void presentMarketsForMatch(MarketsResponseModel responseModel) {
                // Fallback title logic
                assertEquals("Match m99", responseModel.getMatchTitle());
                assertEquals("No markets available.", responseModel.getEmptyStateMessage());
            }

            @Override
            public void presentOrderBook(OrderBookResponseModel r) {
            }

            @Override
            public void presentError(String m) {
                fail("Unexpected error");
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);

        // Act
        interactor.matchSelected("m99");
    }

    @Test
    void testMatchSelected_Failure() {
        // Arrange
        stubFacade.shouldThrowOnMarkets = true;

        ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(MatchesResponseModel r) {
            }

            @Override
            public void presentMarketsForMatch(MarketsResponseModel r) {
                fail("Unexpected success");
            }

            @Override
            public void presentOrderBook(OrderBookResponseModel r) {
            }

            @Override
            public void presentError(String userMessage) {
                assertEquals("There was a problem loading markets.", userMessage);
            }
        };
        interactor = new ViewMarketInteractor(stubFacade, presenter);

        // Act
        interactor.matchSelected("m1");
    }

    @Test
    void testSetStrategy() {
        // Arrange
        // Fix: Added matchId "m1" to constructors
        Market openMarket = new Market("mk1", "m1", "Z-Name", MarketStatus.OPEN);
        Market closedMarket = new Market("mk2", "m1", "A-Name", MarketStatus.CLOSED);
        stubFacade.marketsToReturn.add(openMarket);
        stubFacade.marketsToReturn.add(closedMarket);

        ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(MatchesResponseModel r) {
            }

            @Override
            public void presentMarketsForMatch(MarketsResponseModel responseModel) {
                // With NameSortStrategy, "A-Name" (closed) should come before "Z-Name" (open)
                assertEquals("mk2", responseModel.getMarkets().get(0).getId());
            }

            @Override
            public void presentOrderBook(OrderBookResponseModel r) {
            }

            @Override
            public void presentError(String m) {
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);

        // Act: Change strategy
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

        OrderBook book1 = new OrderBook("mk1", new ArrayList<>(), new ArrayList<>());
        stubFacade.snapshotToReturn = book1;

        ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(MatchesResponseModel r) {
            }

            @Override
            public void presentMarketsForMatch(MarketsResponseModel r) {
            }

            @Override
            public void presentOrderBook(OrderBookResponseModel responseModel) {
                assertNotNull(responseModel.getOrderBook());
                // Assert against the dynamic expected ID
                assertEquals(expectedId[0], responseModel.getOrderBook().getMarketId());
            }

            @Override
            public void presentError(String m) {
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);

        // Act 1: Select market 1
        interactor.marketSelected("mk1");

        // Assert 1
        assertTrue(stubFacade.subscribedMarkets.contains("mk1"));

        // Arrange 2: Prepare to switch
        // Reset spy list
        stubFacade.subscribedMarkets.clear();
        stubFacade.unsubscribedMarkets.clear();

        // Update expected ID for the next call
        expectedId[0] = "mk2";
        stubFacade.snapshotToReturn = new OrderBook("mk2", new ArrayList<>(), new ArrayList<>());

        // Act 2: Select market 2 (should unsubscribe mk1, subscribe mk2)
        interactor.marketSelected("mk2");

        // Assert 2
        assertTrue(stubFacade.unsubscribedMarkets.contains("mk1"));
        assertTrue(stubFacade.subscribedMarkets.contains("mk2"));
    }

    @Test
    void testMarketSelected_Failure() {
        // Arrange
        stubFacade.shouldThrowOnSnapshot = true;

        ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(MatchesResponseModel r) {
            }

            @Override
            public void presentMarketsForMatch(MarketsResponseModel r) {
            }

            @Override
            public void presentOrderBook(OrderBookResponseModel responseModel) {
                assertTrue(responseModel.isReconnecting());
                assertEquals("Reconnecting...", responseModel.getMessage());
            }

            @Override
            public void presentError(String m) {
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);

        // Act
        interactor.marketSelected("mk1");
    }

    @Test
    void testObserverMethods() {
        // Test onOrderBookUpdated
        ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(MatchesResponseModel r) {
            }

            @Override
            public void presentMarketsForMatch(MarketsResponseModel r) {
            }

            @Override
            public void presentOrderBook(OrderBookResponseModel responseModel) {
                if (responseModel.isReconnecting()) return; // ignore reset
                assertFalse(responseModel.isEmpty());
            }

            @Override
            public void presentError(String m) {
            }
        };
        interactor = new ViewMarketInteractor(stubFacade, presenter);

        List<OrderBookEntry> bids = List.of(new OrderBookEntry(Side.BUY, 1.5, 10));
        OrderBook update = new OrderBook("mk1", bids, new ArrayList<>());
        interactor.onOrderBookUpdated(update);

        // Test onConnectionError
        ViewMarketOutputBoundary errorPresenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(MatchesResponseModel r) {
            }

            @Override
            public void presentMarketsForMatch(MarketsResponseModel r) {
            }

            @Override
            public void presentOrderBook(OrderBookResponseModel responseModel) {
                assertTrue(responseModel.isReconnecting());
                assertEquals("Lost connection", responseModel.getMessage());
            }

            @Override
            public void presentError(String m) {
            }
        };
        interactor = new ViewMarketInteractor(stubFacade, errorPresenter);
        interactor.onConnectionError("Lost connection");

        // Test onConnectionRestored
        ViewMarketOutputBoundary restorePresenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(MatchesResponseModel r) {
            }

            @Override
            public void presentMarketsForMatch(MarketsResponseModel r) {
            }

            @Override
            public void presentOrderBook(OrderBookResponseModel responseModel) {
                assertFalse(responseModel.isReconnecting());
                assertEquals("Connection restored", responseModel.getMessage());
            }

            @Override
            public void presentError(String m) {
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
            if (shouldThrowOnMatches) throw new RepositoryException("DB Error");
            return matchesToReturn;
        }

        @Override
        public void refreshApi() throws RepositoryException {
            if (shouldThrowOnRefresh) throw new RepositoryException("API Error");
            refreshCalled = true;
        }

        @Override
        public List<Market> getMarketsForMatch(String matchId) throws RepositoryException {
            if (shouldThrowOnMarkets) throw new RepositoryException("DB Error");
            return marketsToReturn;
        }

        @Override
        public OrderBook getOrderBookSnapshot(String marketId) throws RepositoryException {
            if (shouldThrowOnSnapshot) throw new RepositoryException("DB Error");
            if (snapshotToReturn == null) {
                return new OrderBook(marketId, new ArrayList<>(), new ArrayList<>());
            }
            return snapshotToReturn;
        }

        @Override
        public void subscribeToOrderBook(String marketId, OrderBookSubscriber subscriber) {
            subscribedMarkets.add(marketId);
        }

        @Override
        public void unsubscribeFromOrderBook(String marketId, OrderBookSubscriber subscriber) {
            unsubscribedMarkets.add(marketId);
        }
    }
}
