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

class ViewMarketInteractorTest {

    private static final MatchRepository DUMMY_MATCH_REPO = () -> new ArrayList<>();
    private static final MarketRepository DUMMY_MARKET_REPO = matchId -> new ArrayList<>();

    private StubMarketDataFacade stubFacade;
    private ViewMarketInteractor interactor;

    @BeforeEach
    void setUp() {
        stubFacade = new StubMarketDataFacade();
    }

    @Test
    void testLoadMatchesSuccess() {
        stubFacade.addMatch(new Match("match1", "Home", "Away",
            MatchStatus.UPCOMING, LocalDateTime.now()));

        final ViewMarketOutputBoundary successPresenter = new TestOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel responseModel) {
                assertEquals(1, responseModel.getMatches().size());
                assertEquals("Home vs Away", responseModel.getMatches().get(0).getLabel());
                assertNull(responseModel.getEmptyStateMessage());
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, successPresenter);
        interactor.loadMatches();
    }

    @Test
    void testLoadMatchesEmpty() {
        stubFacade.clearMatches();

        final ViewMarketOutputBoundary emptyPresenter = new TestOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel responseModel) {
                assertTrue(responseModel.getMatches().isEmpty());
                assertEquals("No upcoming games", responseModel.getEmptyStateMessage());
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, emptyPresenter);
        interactor.loadMatches();
    }

    @Test
    void testLoadMatchesFailure() {
        stubFacade.setShouldThrowOnMatches(true);

        final ViewMarketOutputBoundary errorPresenter = new TestOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) {
                fail("Unexpected success");
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
    void testRefreshFromApiSuccess() {
        final ViewMarketOutputBoundary presenter = new TestOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel responseModel) {
                assertNotNull(responseModel);
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);
        interactor.refreshFromApi();
        assertTrue(stubFacade.isRefreshCalled());
    }

    @Test
    void testRefreshFromApiFailure() {
        stubFacade.setShouldThrowOnRefresh(true);

        final ViewMarketOutputBoundary presenter = new TestOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) {
                fail("Unexpected success");
            }

            @Override
            public void presentError(final String userMessage) {
                assertTrue(userMessage.startsWith("Error refreshing from API"));
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);
        interactor.refreshFromApi();
    }

    @Test
    void testMatchSelectedWithCachedMatchAndMarkets() {
        final Match match = new Match("m1", "Raptors", "Celtics",
            MatchStatus.UPCOMING, LocalDateTime.now());
        stubFacade.addMatch(match);
        final Market openMarket = new Market("mk1", "m1", "Moneyline", MarketStatus.OPEN);
        final Market closedMarket = new Market("mk2", "m1", "Total", MarketStatus.CLOSED);
        stubFacade.addMarket(closedMarket);
        stubFacade.addMarket(openMarket);

        final ViewMarketOutputBoundary presenter = new TestOutputBoundary() {
            @Override
            public void presentMatches(final MatchesResponseModel r) {
                // Do nothing
            }

            @Override
            public void presentMarketsForMatch(final MarketsResponseModel responseModel) {
                assertEquals("m1", responseModel.getMatchId());
                assertEquals("Raptors vs Celtics", responseModel.getMatchTitle());

                final List<MarketSummary> markets = responseModel.getMarkets();
                assertEquals(2, markets.size());
                assertEquals("mk1", markets.get(0).getId());
                assertTrue(markets.get(0).getName().contains("Moneyline"));
                assertTrue(markets.get(0).toString().contains("HOT"),
                    "Decorator should add HOT tag");
                assertFalse(markets.get(1).toString().contains("HOT"));
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);
        interactor.loadMatches();
        interactor.matchSelected("m1");
    }

    @Test
    void testMatchSelectedMatchNotInCache() {
        final ViewMarketOutputBoundary presenter = new TestOutputBoundary() {
            @Override
            public void presentMarketsForMatch(final MarketsResponseModel responseModel) {
                assertEquals("Match m99", responseModel.getMatchTitle());
                assertEquals("No markets available.", responseModel.getEmptyStateMessage());
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);
        interactor.matchSelected("m99");
    }

    @Test
    void testMatchSelectedFailure() {
        stubFacade.setShouldThrowOnMarkets(true);

        final ViewMarketOutputBoundary presenter = new TestOutputBoundary() {
            @Override
            public void presentMarketsForMatch(final MarketsResponseModel r) {
                fail("Unexpected success");
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
        final Market openMarket = new Market("mk1", "m1", "Z-Name", MarketStatus.OPEN);
        final Market closedMarket = new Market("mk2", "m1", "A-Name", MarketStatus.CLOSED);
        stubFacade.addMarket(openMarket);
        stubFacade.addMarket(closedMarket);

        final ViewMarketOutputBoundary presenter = new TestOutputBoundary() {
            @Override
            public void presentMarketsForMatch(final MarketsResponseModel responseModel) {
                assertEquals("mk2", responseModel.getMarkets().get(0).getId());
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);
        interactor.setMarketSortStrategy(markets -> {
            markets.sort((market1, market2) -> {
                return market1.getName().compareTo(market2.getName());
            });
        });
        interactor.matchSelected("m1");
    }

    @Test
    void testMarketSelectedSuccessAndSwitching() {
        final String[] expectedId = {"mk1"};
        final OrderBook book1 = new OrderBook("mk1", new ArrayList<>(), new ArrayList<>());
        stubFacade.setSnapshotToReturn(book1);

        final ViewMarketOutputBoundary presenter = new TestOutputBoundary() {
            @Override
            public void presentOrderBook(final OrderBookResponseModel responseModel) {
                assertNotNull(responseModel.getOrderBook());
                assertEquals(expectedId[0], responseModel.getOrderBook().getMarketId());
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);
        interactor.marketSelected("mk1");
        assertTrue(stubFacade.getSubscribedMarkets().contains("mk1"));

        stubFacade.clearSubscriptions();
        expectedId[0] = "mk2";
        stubFacade.setSnapshotToReturn(new OrderBook("mk2", new ArrayList<>(), new ArrayList<>()));
        interactor.marketSelected("mk2");
        assertTrue(stubFacade.getUnsubscribedMarkets().contains("mk1"));
        assertTrue(stubFacade.getSubscribedMarkets().contains("mk2"));
    }

    @Test
    void testMarketSelectedFailure() {
        stubFacade.setShouldThrowOnSnapshot(true);

        final ViewMarketOutputBoundary presenter = new TestOutputBoundary() {
            @Override
            public void presentOrderBook(final OrderBookResponseModel responseModel) {
                assertTrue(responseModel.isReconnecting());
                assertEquals("Reconnecting...", responseModel.getMessage());
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);
        interactor.marketSelected("mk1");
    }

    @Test
    void testObserverMethods() {
        final ViewMarketOutputBoundary presenter = new TestOutputBoundary() {
            @Override
            public void presentOrderBook(final OrderBookResponseModel responseModel) {
                if (!responseModel.isReconnecting()) {
                    assertFalse(responseModel.isEmpty());
                }
            }
        };
        interactor = new ViewMarketInteractor(stubFacade, presenter);

        final List<OrderBookEntry> bids = List.of(new OrderBookEntry(Side.BUY, 1.5, 10));
        final OrderBook update = new OrderBook("mk1", bids, new ArrayList<>());
        interactor.onOrderBookUpdated(update);

        final ViewMarketOutputBoundary errorPresenter = new TestOutputBoundary() {
            @Override
            public void presentOrderBook(final OrderBookResponseModel responseModel) {
                assertTrue(responseModel.isReconnecting());
                assertEquals("Lost connection", responseModel.getMessage());
            }
        };
        interactor = new ViewMarketInteractor(stubFacade, errorPresenter);
        interactor.onConnectionError("Lost connection");

        final ViewMarketOutputBoundary restorePresenter = new TestOutputBoundary() {
            @Override
            public void presentOrderBook(final OrderBookResponseModel responseModel) {
                assertFalse(responseModel.isReconnecting());
                assertEquals("Connection restored", responseModel.getMessage());
            }
        };
        interactor = new ViewMarketInteractor(stubFacade, restorePresenter);
        interactor.onConnectionRestored();
    }

    @Test
    void testMarketSelectedSameMarketTwiceBranchCoverage() {
        stubFacade.setSnapshotToReturn(new OrderBook("mk1",
            new ArrayList<>(), new ArrayList<>()));
        final ViewMarketOutputBoundary presenter = new TestOutputBoundary() {
            @Override
            public void presentOrderBook(final OrderBookResponseModel responseModel) {
                // Do nothing
            }
        };
        interactor = new ViewMarketInteractor(stubFacade, presenter);

        interactor.marketSelected("mk1");
        stubFacade.clearSubscriptions();

        interactor.marketSelected("mk1");

        assertFalse(stubFacade.getUnsubscribedMarkets().contains("mk1"));
    }

    @Test
    void testOnOrderBookUpdatedEmptyBranchCoverage() {
        final ViewMarketOutputBoundary presenter = new TestOutputBoundary() {
            @Override
            public void presentOrderBook(final OrderBookResponseModel responseModel) {
                assertTrue(responseModel.isEmpty());
                assertEquals("No orders yet", responseModel.getMessage());
            }
        };
        interactor = new ViewMarketInteractor(stubFacade, presenter);

        interactor.onOrderBookUpdated(new OrderBook("m1",
            new ArrayList<>(), new ArrayList<>()));
    }

    @Test
    void testOnConnectionErrorNullMessageBranchCoverage() {
        final ViewMarketOutputBoundary presenter = new TestOutputBoundary() {
            @Override
            public void presentOrderBook(final OrderBookResponseModel responseModel) {
                assertTrue(responseModel.isReconnecting());
                assertEquals("Reconnecting...", responseModel.getMessage());
            }
        };
        interactor = new ViewMarketInteractor(stubFacade, presenter);

        interactor.onConnectionError(null);
    }

    @Test
    void testMarketSelectedWithPopulatedOrderBookBranchCoverage() {
        final List<OrderBookEntry> bids = new ArrayList<>();
        bids.add(new OrderBookEntry(Side.BUY, 10.0, 1.0));
        final OrderBook populatedBook = new OrderBook("mk1", bids, new ArrayList<>());

        stubFacade.setSnapshotToReturn(populatedBook);

        final ViewMarketOutputBoundary presenter = new TestOutputBoundary() {
            @Override
            public void presentOrderBook(final OrderBookResponseModel responseModel) {
                assertFalse(responseModel.isEmpty());
                assertNull(responseModel.getMessage());
            }
        };

        interactor = new ViewMarketInteractor(stubFacade, presenter);

        interactor.marketSelected("mk1");
    }

    /**
     * Helper class to reduce anonymous inner class length in tests.
     */
    private static class TestOutputBoundary implements ViewMarketOutputBoundary {
        @Override
        public void presentMatches(final MatchesResponseModel responseModel) {
            fail("Unexpected call to presentMatches");
        }

        @Override
        public void presentMarketsForMatch(final MarketsResponseModel responseModel) {
            fail("Unexpected call to presentMarketsForMatch");
        }

        @Override
        public void presentOrderBook(final OrderBookResponseModel responseModel) {
            fail("Unexpected call to presentOrderBook");
        }

        @Override
        public void presentError(final String error) {
            fail("Unexpected error: " + error);
        }
    }

    private static class StubMarketDataFacade extends MarketDataFacade {
        private boolean shouldThrowOnMatches;
        private boolean shouldThrowOnRefresh;
        private boolean shouldThrowOnMarkets;
        private boolean shouldThrowOnSnapshot;

        private boolean refreshCalled;

        private final List<Match> matchesToReturn = new ArrayList<>();
        private final List<Market> marketsToReturn = new ArrayList<>();
        private OrderBook snapshotToReturn;

        private final List<String> subscribedMarkets = new ArrayList<>();
        private final List<String> unsubscribedMarkets = new ArrayList<>();

        StubMarketDataFacade() {
            super(DUMMY_MATCH_REPO, DUMMY_MARKET_REPO, null);
        }

        void setShouldThrowOnMatches(final boolean shouldThrow) {
            this.shouldThrowOnMatches = shouldThrow;
        }

        void setShouldThrowOnRefresh(final boolean shouldThrow) {
            this.shouldThrowOnRefresh = shouldThrow;
        }

        void setShouldThrowOnMarkets(final boolean shouldThrow) {
            this.shouldThrowOnMarkets = shouldThrow;
        }

        void setShouldThrowOnSnapshot(final boolean shouldThrow) {
            this.shouldThrowOnSnapshot = shouldThrow;
        }

        boolean isRefreshCalled() {
            return refreshCalled;
        }

        void addMatch(final Match match) {
            matchesToReturn.add(match);
        }

        void clearMatches() {
            matchesToReturn.clear();
        }

        void addMarket(final Market market) {
            marketsToReturn.add(market);
        }

        void setSnapshotToReturn(final OrderBook snapshot) {
            this.snapshotToReturn = snapshot;
        }

        List<String> getSubscribedMarkets() {
            return subscribedMarkets;
        }

        List<String> getUnsubscribedMarkets() {
            return unsubscribedMarkets;
        }

        void clearSubscriptions() {
            subscribedMarkets.clear();
            unsubscribedMarkets.clear();
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
            final OrderBook result;
            if (snapshotToReturn == null) {
                result = new OrderBook(marketId, new ArrayList<>(), new ArrayList<>());
            }
            else {
                result = snapshotToReturn;
            }
            return result;
        }

        @Override
        public void subscribeToOrderBook(final String marketId,
                                         final OrderBookSubscriber subscriber) {
            subscribedMarkets.add(marketId);
        }

        @Override
        public void unsubscribeFromOrderBook(final String marketId,
                                             final OrderBookSubscriber subscriber) {
            unsubscribedMarkets.add(marketId);
        }
    }
}
