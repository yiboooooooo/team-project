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

import stakemate.data_access.in_memory.InMemoryMatchRepository;
import stakemate.entity.Market;
import stakemate.entity.MarketStatus;
import stakemate.entity.Match;
import stakemate.entity.MatchStatus;
import stakemate.entity.OrderBook;
import stakemate.entity.OrderBookEntry;
import stakemate.entity.Side;
import stakemate.use_case.view_market.builder.MarketsResponseModelBuilder;
import stakemate.use_case.view_market.decorator.HotMarketDecorator;
import stakemate.use_case.view_market.facade.MarketDataFacade;
import stakemate.use_case.view_market.strategy.MarketSortStrategy;
import stakemate.use_case.view_market.strategy.NameSortStrategy;
import stakemate.use_case.view_market.strategy.StatusSortStrategy;

class ViewMarketInteractorTest {

    // Define dummy repositories to satisfy Facade constructor
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
    // Existing Logic Tests (ViewMarketInteractor)
    // =========================================================================

    @Test
    void testLoadMatches_Success() {
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

    @Test
    void testRefreshFromApi_Success() {
        final ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel responseModel) {
                assertNotNull(responseModel);
            }

            @Override
            public void presentMarketsForMatch(final MarketsResponseModel r) {}
            @Override
            public void presentOrderBook(final OrderBookResponseModel r) {}
            @Override
            public void presentError(final String m) { fail("Unexpected error"); }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);
        interactor.refreshFromApi();
        assertTrue(stubFacade.refreshCalled);
    }

    @Test
    void testRefreshFromApi_Failure() {
        stubFacade.shouldThrowOnRefresh = true;

        final ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) { fail("Unexpected success"); }
            @Override
            public void presentMarketsForMatch(final MarketsResponseModel r) {}
            @Override
            public void presentOrderBook(final OrderBookResponseModel r) {}
            @Override
            public void presentError(final String userMessage) {
                assertTrue(userMessage.startsWith("Error refreshing from API"));
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);
        interactor.refreshFromApi();
    }

    @Test
    void testMatchSelected_WithCachedMatch_AndMarkets() {
        final Match match = new Match("m1", "Raptors", "Celtics", MatchStatus.UPCOMING, LocalDateTime.now());
        stubFacade.matchesToReturn.add(match);
        final Market openMarket = new Market("mk1", "m1", "Moneyline", MarketStatus.OPEN);
        final Market closedMarket = new Market("mk2", "m1", "Total", MarketStatus.CLOSED);
        stubFacade.marketsToReturn.add(closedMarket);
        stubFacade.marketsToReturn.add(openMarket);

        final ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) {}

            @Override
            public void presentMarketsForMatch(final MarketsResponseModel responseModel) {
                assertEquals("m1", responseModel.getMatchId());
                assertEquals("Raptors vs Celtics", responseModel.getMatchTitle());

                final List<MarketSummary> markets = responseModel.getMarkets();
                assertEquals(2, markets.size());
                assertEquals("mk1", markets.get(0).getId());
                assertTrue(markets.get(0).getName().contains("Moneyline"));
                assertTrue(markets.get(0).toString().contains("HOT"), "Decorator should add HOT tag");
                assertFalse(markets.get(1).toString().contains("HOT"));
            }

            @Override
            public void presentOrderBook(final OrderBookResponseModel r) {}
            @Override
            public void presentError(final String m) { fail("Unexpected error"); }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);
        interactor.loadMatches();
        interactor.matchSelected("m1");
    }

    @Test
    void testMatchSelected_MatchNotInCache() {
        final ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) {}

            @Override
            public void presentMarketsForMatch(final MarketsResponseModel responseModel) {
                assertEquals("Match m99", responseModel.getMatchTitle());
                assertEquals("No markets available.", responseModel.getEmptyStateMessage());
            }

            @Override
            public void presentOrderBook(final OrderBookResponseModel r) {}
            @Override
            public void presentError(final String m) { fail("Unexpected error"); }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);
        interactor.matchSelected("m99");
    }

    @Test
    void testMatchSelected_Failure() {
        stubFacade.shouldThrowOnMarkets = true;

        final ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) {}
            @Override
            public void presentMarketsForMatch(final MarketsResponseModel r) { fail("Unexpected success"); }
            @Override
            public void presentOrderBook(final OrderBookResponseModel r) {}
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
        final Market openMarket = new Market("mk1", "m1", "Z-Name", MarketStatus.OPEN);
        final Market closedMarket = new Market("mk2", "m1", "A-Name", MarketStatus.CLOSED);
        stubFacade.marketsToReturn.add(openMarket);
        stubFacade.marketsToReturn.add(closedMarket);

        final ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) {}
            @Override
            public void presentMarketsForMatch(final MarketsResponseModel responseModel) {
                assertEquals("mk2", responseModel.getMarkets().get(0).getId());
            }
            @Override
            public void presentOrderBook(final OrderBookResponseModel r) {}
            @Override
            public void presentError(final String m) {}
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);
        interactor.setMarketSortStrategy(new NameSortStrategy());
        interactor.matchSelected("m1");
    }

    @Test
    void testMarketSelected_Success_And_Switching() {
        final String[] expectedId = {"mk1"};
        final OrderBook book1 = new OrderBook("mk1", new ArrayList<>(), new ArrayList<>());
        stubFacade.snapshotToReturn = book1;

        final ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) {}
            @Override
            public void presentMarketsForMatch(final MarketsResponseModel r) {}
            @Override
            public void presentOrderBook(final OrderBookResponseModel responseModel) {
                assertNotNull(responseModel.getOrderBook());
                assertEquals(expectedId[0], responseModel.getOrderBook().getMarketId());
            }
            @Override
            public void presentError(final String m) {}
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);
        interactor.marketSelected("mk1");
        assertTrue(stubFacade.subscribedMarkets.contains("mk1"));

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
        stubFacade.shouldThrowOnSnapshot = true;

        final ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) {}
            @Override
            public void presentMarketsForMatch(final MarketsResponseModel r) {}
            @Override
            public void presentOrderBook(final OrderBookResponseModel responseModel) {
                assertTrue(responseModel.isReconnecting());
                assertEquals("Reconnecting...", responseModel.getMessage());
            }
            @Override
            public void presentError(final String m) {}
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);
        interactor.marketSelected("mk1");
    }

    @Test
    void testObserverMethods() {
        final ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) {}
            @Override
            public void presentMarketsForMatch(final MarketsResponseModel r) {}
            @Override
            public void presentOrderBook(final OrderBookResponseModel responseModel) {
                if (responseModel.isReconnecting()) return;
                assertFalse(responseModel.isEmpty());
            }
            @Override
            public void presentError(final String m) {}
        };
        interactor = new ViewMarketInteractor(stubFacade, presenter);

        final List<OrderBookEntry> bids = List.of(new OrderBookEntry(Side.BUY, 1.5, 10));
        final OrderBook update = new OrderBook("mk1", bids, new ArrayList<>());
        interactor.onOrderBookUpdated(update);

        final ViewMarketOutputBoundary errorPresenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) {}
            @Override
            public void presentMarketsForMatch(final MarketsResponseModel r) {}
            @Override
            public void presentOrderBook(final OrderBookResponseModel responseModel) {
                assertTrue(responseModel.isReconnecting());
                assertEquals("Lost connection", responseModel.getMessage());
            }
            @Override
            public void presentError(final String m) {}
        };
        interactor = new ViewMarketInteractor(stubFacade, errorPresenter);
        interactor.onConnectionError("Lost connection");

        final ViewMarketOutputBoundary restorePresenter = new ViewMarketOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) {}
            @Override
            public void presentMarketsForMatch(final MarketsResponseModel r) {}
            @Override
            public void presentOrderBook(final OrderBookResponseModel responseModel) {
                assertFalse(responseModel.isReconnecting());
                assertEquals("Connection restored", responseModel.getMessage());
            }
            @Override
            public void presentError(final String m) {}
        };
        interactor = new ViewMarketInteractor(stubFacade, restorePresenter);
        interactor.onConnectionRestored();
    }

    // =========================================================================
    // NEW: Branch Coverage Tests (to reach 100%)
    // =========================================================================

    @Test
    void testMarketSelected_SameMarketTwice_BranchCoverage() {
        // Setup
        stubFacade.snapshotToReturn = new OrderBook("mk1", new ArrayList<>(), new ArrayList<>());
        final ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override public void presentMatches(MatchesResponseModel r) {}
            @Override public void presentMarketsForMatch(MarketsResponseModel r) {}
            @Override public void presentOrderBook(OrderBookResponseModel r) {}
            @Override public void presentError(String m) {}
        };
        interactor = new ViewMarketInteractor(stubFacade, presenter);

        // Select "mk1"
        interactor.marketSelected("mk1");
        stubFacade.unsubscribedMarkets.clear(); // Clear history

        // Select "mk1" AGAIN
        interactor.marketSelected("mk1");

        // Verify: The unsubscribe logic should NOT have triggered because it's the same ID
        assertFalse(stubFacade.unsubscribedMarkets.contains("mk1"));
    }

    @Test
    void testOnOrderBookUpdated_Empty_BranchCoverage() {
        final ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override public void presentMatches(MatchesResponseModel r) {}
            @Override public void presentMarketsForMatch(MarketsResponseModel r) {}
            @Override public void presentOrderBook(OrderBookResponseModel responseModel) {
                // Verify the "Empty" branch logic
                assertTrue(responseModel.isEmpty());
                assertEquals("No orders yet", responseModel.getMessage());
            }
            @Override public void presentError(String m) {}
        };
        interactor = new ViewMarketInteractor(stubFacade, presenter);

        // Pass an empty OrderBook to trigger the 'if (empty)' branch
        interactor.onOrderBookUpdated(new OrderBook("m1", new ArrayList<>(), new ArrayList<>()));
    }

    @Test
    void testOnConnectionError_NullMessage_BranchCoverage() {
        final ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override public void presentMatches(MatchesResponseModel r) {}
            @Override public void presentMarketsForMatch(MarketsResponseModel r) {}
            @Override public void presentOrderBook(OrderBookResponseModel responseModel) {
                // Verify the 'else' branch where message is null
                assertTrue(responseModel.isReconnecting());
                assertEquals("Reconnecting...", responseModel.getMessage());
            }
            @Override public void presentError(String m) {}
        };
        interactor = new ViewMarketInteractor(stubFacade, presenter);

        // Pass null to trigger the default message logic
        interactor.onConnectionError(null);
    }

    // =========================================================================
    // NEW: Coverage Tests to reach 100% (Facade, Builder, Strategies, Models)
    // =========================================================================

    @Test
    void testMarketDataFacade_Coverage() throws RepositoryException {
        // Create REAL Facade (not stub) to test its internal methods
        MatchRepository matchRepo = () -> List.of(new Match("m1", "A", "B", MatchStatus.LIVE, LocalDateTime.now()));
        MarketRepository marketRepo = id -> List.of(new Market("mk1", "m1", "Name", MarketStatus.OPEN));

        // Mock Gateway using functional interface or anonymous class
        OrderBookGateway gateway = new OrderBookGateway() {
            @Override
            public OrderBook getSnapshot(String marketId) {
                return new OrderBook(marketId, new ArrayList<>(), new ArrayList<>());
            }
            @Override
            public void subscribe(String marketId, OrderBookSubscriber subscriber) {}
            @Override
            public void unsubscribe(String marketId, OrderBookSubscriber subscriber) {}
        };

        MarketDataFacade realFacade = new MarketDataFacade(matchRepo, marketRepo, gateway);

        // Execute all facade methods
        assertNotNull(realFacade.getAllMatches());
        assertNotNull(realFacade.getMarketsForMatch("m1"));
        assertNotNull(realFacade.getOrderBookSnapshot("mk1"));
        realFacade.subscribeToOrderBook("mk1", null);
        realFacade.unsubscribeFromOrderBook("mk1", null);

        // Test refreshApi with a non-InMemory repo (should do nothing but not crash)
        realFacade.refreshApi();

        // Test refreshApi with InMemoryMatchRepository (to cover the casting line)
        // We mock InMemoryMatchRepository partially
        InMemoryMatchRepository inMemoryRepo = new InMemoryMatchRepository(null, null) {
            @Override
            public void syncWithApiData() throws RepositoryException {
                // Do nothing for test
            }
        };
        MarketDataFacade facadeWithInMemory = new MarketDataFacade(inMemoryRepo, marketRepo, gateway);
        facadeWithInMemory.refreshApi();
    }

    @Test
    void testMarketsResponseModelBuilder_Coverage() {
        MarketsResponseModelBuilder builder = new MarketsResponseModelBuilder();
        MarketSummary market = new MarketSummary("id", "name", "Open", true);

        // Chain all methods
        MarketsResponseModel model = builder
            .setMatchId("m1")
            .setMatchTitle("Title")
            .addMarket(market)
            .setMarkets(List.of(market))
            .setEmptyStateMessage("Empty")
            .build();

        assertNotNull(model);
        assertEquals("m1", model.getMatchId());
        assertEquals("Title", model.getMatchTitle());
        assertEquals("Empty", model.getEmptyStateMessage());

        // Test default empty message build path
        MarketsResponseModel emptyModel = new MarketsResponseModelBuilder().build();
        assertEquals("No markets available.", emptyModel.getEmptyStateMessage());
    }

    @Test
    void testDataClasses_Coverage() {
        // MatchSummary
        MatchSummary match = new MatchSummary("id", "label", "LIVE");
        assertEquals("id", match.getId());
        assertEquals("label", match.getLabel());
        assertEquals("LIVE", match.getStatusLabel());
        assertNotNull(match.toString());

        // MarketSummary
        MarketSummary market = new MarketSummary("id", "name", "Open", true);
        assertEquals("id", market.getId());
        assertEquals("name", market.getName());
        assertEquals("Open", market.getStatusLabel());
        assertTrue(market.isBuySellEnabled());
        assertNotNull(market.toString());

        // HotMarketDecorator
        HotMarketDecorator hot = new HotMarketDecorator(market);
        assertTrue(hot.toString().contains("HOT"));

        // OrderBookResponseModel
        OrderBookResponseModel obResp = new OrderBookResponseModel(null, true, false, "msg");
        assertNull(obResp.getOrderBook());
        assertTrue(obResp.isEmpty());
        assertFalse(obResp.isReconnecting());
        assertEquals("msg", obResp.getMessage());
    }

    @Test
    void testStrategies_Coverage() {
        List<MarketSummary> list = new ArrayList<>();
        list.add(new MarketSummary("1", "B-Name", "Closed", false));
        list.add(new MarketSummary("2", "A-Name", "Open", true));

        // Name Sort
        MarketSortStrategy nameSort = new NameSortStrategy();
        nameSort.sort(list);
        assertEquals("A-Name", list.get(0).getName());

        // Status Sort
        MarketSortStrategy statusSort = new StatusSortStrategy();
        statusSort.sort(list);
        // Open comes before Closed in status sort (due to reversed comparator on status string)
        // Note: "Open" > "Closed" alphabetically, so reversed puts Open first.
        assertEquals("Open", list.get(0).getStatusLabel());
    }

    @Test
    void testMarketSelected_WithPopulatedOrderBook_BranchCoverage() throws RepositoryException {
        // Setup Facade to return a populated OrderBook (not empty)
        List<OrderBookEntry> bids = new ArrayList<>();
        bids.add(new OrderBookEntry(Side.BUY, 10.0, 1.0));
        OrderBook populatedBook = new OrderBook("mk1", bids, new ArrayList<>());

        stubFacade.snapshotToReturn = populatedBook;

        final ViewMarketOutputBoundary presenter = new ViewMarketOutputBoundary() {
            @Override public void presentMatches(MatchesResponseModel r) {}
            @Override public void presentMarketsForMatch(MarketsResponseModel r) {}

            @Override
            public void presentOrderBook(OrderBookResponseModel responseModel) {
                // Verify that the "else" branch (msg = null) executed
                assertFalse(responseModel.isEmpty());
                assertNull(responseModel.getMessage()); // This assertion covers the missing line
            }

            @Override public void presentError(String m) {}
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);

        // Action
        interactor.marketSelected("mk1");
    }

    // =========================================================================
    // Internal Stub Class (Keep existing for Interactor tests)
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
