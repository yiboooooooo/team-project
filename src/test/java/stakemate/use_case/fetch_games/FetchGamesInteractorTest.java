package stakemate.use_case.fetch_games;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import stakemate.data_access.api.OddsApiResponseAdapter;
import stakemate.entity.Game;
import stakemate.entity.GameStatus;

/**
 * Tests for the FetchGames Interactor.
 */
public class FetchGamesInteractorTest {

    private StubOddsApiGateway stubApiGateway;
    private OddsApiResponseAdapter responseAdapter;
    private StubGameRepository stubRepository;
    private FetchGamesInteractor interactor;

    @BeforeEach
    void setUp() {
        stubApiGateway = new StubOddsApiGateway();
        responseAdapter = new OddsApiResponseAdapter();
        stubRepository = new StubGameRepository();
    }

    @Test
    void testFetchAndUpdateGamesSuccess() {
        final LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        final OddsApiEvent event = new OddsApiEvent("evt1", "basketball_nba", futureTime, "Lakers", "Celtics");
        stubApiGateway.setEventsToReturn(List.of(event));

        final FetchGamesOutputBoundary successPresenter = new FetchGamesOutputBoundary() {
            private boolean inProgressCalled = false;

            @Override
            public void presentFetchInProgress() {
                inProgressCalled = true;
            }

            @Override
            public void presentFetchSuccess(final FetchGamesResponseModel responseModel) {
                if (!inProgressCalled) {
                    fail("presentFetchInProgress should be called before success");
                }
                assertEquals("basketball_nba", responseModel.getSport());
                assertEquals(1, responseModel.getGamesFetched());
                assertEquals(1, responseModel.getGamesSaved());
            }

            @Override
            public void presentFetchError(final String errorMessage) {
                fail("Unexpected error: " + errorMessage);
            }

            @Override
            public void presentSearchResults(final List<Game> games, final String query) {
                fail("Unexpected search results");
            }
        };

        interactor = new FetchGamesInteractor(stubApiGateway, responseAdapter, stubRepository, successPresenter);
        interactor.fetchAndUpdateGames("basketball_nba", "us", LocalDate.now());
    }

    @Test
    void testFetchAndUpdateGamesWithNullSport() {
        final FetchGamesOutputBoundary failPresenter = new FetchGamesOutputBoundary() {
            @Override
            public void presentFetchInProgress() {
                // Expected
            }

            @Override
            public void presentFetchSuccess(final FetchGamesResponseModel responseModel) {
                fail("Unexpected success");
            }

            @Override
            public void presentFetchError(final String errorMessage) {
                assertEquals("Sport parameter is required", errorMessage);
            }

            @Override
            public void presentSearchResults(final List<Game> games, final String query) {
                fail("Unexpected search results");
            }
        };

        interactor = new FetchGamesInteractor(stubApiGateway, responseAdapter, stubRepository, failPresenter);
        interactor.fetchAndUpdateGames(null, "us", LocalDate.now());
    }

    @Test
    void testFetchAndUpdateGamesWithEmptySport() {
        final FetchGamesOutputBoundary failPresenter = new FetchGamesOutputBoundary() {
            @Override
            public void presentFetchInProgress() {
                // Expected
            }

            @Override
            public void presentFetchSuccess(final FetchGamesResponseModel responseModel) {
                fail("Unexpected success");
            }

            @Override
            public void presentFetchError(final String errorMessage) {
                assertEquals("Sport parameter is required", errorMessage);
            }

            @Override
            public void presentSearchResults(final List<Game> games, final String query) {
                fail("Unexpected search results");
            }
        };

        interactor = new FetchGamesInteractor(stubApiGateway, responseAdapter, stubRepository, failPresenter);
        interactor.fetchAndUpdateGames("", "us", LocalDate.now());
    }

    @Test
    void testFetchAndUpdateGamesWithNullApiGateway() {
        final FetchGamesOutputBoundary failPresenter = new FetchGamesOutputBoundary() {
            @Override
            public void presentFetchInProgress() {
                // Expected
            }

            @Override
            public void presentFetchSuccess(final FetchGamesResponseModel responseModel) {
                fail("Unexpected success");
            }

            @Override
            public void presentFetchError(final String errorMessage) {
                assertEquals("API gateway not configured. Please set ODDS_API_KEY.", errorMessage);
            }

            @Override
            public void presentSearchResults(final List<Game> games, final String query) {
                fail("Unexpected search results");
            }
        };

        interactor = new FetchGamesInteractor(null, responseAdapter, stubRepository, failPresenter);
        interactor.fetchAndUpdateGames("basketball_nba", "us", LocalDate.now());
    }

    @Test
    void testFetchAndUpdateGamesEmptyApiResponse() {
        stubApiGateway.setEventsToReturn(new ArrayList<>());

        final FetchGamesOutputBoundary successPresenter = new FetchGamesOutputBoundary() {
            @Override
            public void presentFetchInProgress() {
                // Expected
            }

            @Override
            public void presentFetchSuccess(final FetchGamesResponseModel responseModel) {
                assertEquals(0, responseModel.getGamesFetched());
                assertEquals(0, responseModel.getGamesSaved());
                assertEquals("No events found for the specified criteria.", responseModel.getMessage());
            }

            @Override
            public void presentFetchError(final String errorMessage) {
                fail("Unexpected error: " + errorMessage);
            }

            @Override
            public void presentSearchResults(final List<Game> games, final String query) {
                fail("Unexpected search results");
            }
        };

        interactor = new FetchGamesInteractor(stubApiGateway, responseAdapter, stubRepository, successPresenter);
        interactor.fetchAndUpdateGames("basketball_nba", "us", LocalDate.now());
    }

    @Test
    void testFetchAndUpdateGamesApiException() {
        stubApiGateway.setShouldThrowException(true);

        final FetchGamesOutputBoundary failPresenter = new FetchGamesOutputBoundary() {
            @Override
            public void presentFetchInProgress() {
                // Expected
            }

            @Override
            public void presentFetchSuccess(final FetchGamesResponseModel responseModel) {
                fail("Unexpected success");
            }

            @Override
            public void presentFetchError(final String errorMessage) {
                assertEquals("API error: API failure", errorMessage);
            }

            @Override
            public void presentSearchResults(final List<Game> games, final String query) {
                fail("Unexpected search results");
            }
        };

        interactor = new FetchGamesInteractor(stubApiGateway, responseAdapter, stubRepository, failPresenter);
        interactor.fetchAndUpdateGames("basketball_nba", "us", LocalDate.now());
    }

    @Test
    void testFetchAndUpdateGamesRepositoryException() {
        final LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        final OddsApiEvent event = new OddsApiEvent("evt1", "basketball_nba", futureTime, "Lakers", "Celtics");
        stubApiGateway.setEventsToReturn(List.of(event));
        stubRepository.setShouldThrowException(true);

        final FetchGamesOutputBoundary failPresenter = new FetchGamesOutputBoundary() {
            @Override
            public void presentFetchInProgress() {
                // Expected
            }

            @Override
            public void presentFetchSuccess(final FetchGamesResponseModel responseModel) {
                fail("Unexpected success");
            }

            @Override
            public void presentFetchError(final String errorMessage) {
                assertEquals("Database error: Repository failure", errorMessage);
            }

            @Override
            public void presentSearchResults(final List<Game> games, final String query) {
                fail("Unexpected search results");
            }
        };

        interactor = new FetchGamesInteractor(stubApiGateway, responseAdapter, stubRepository, failPresenter);
        interactor.fetchAndUpdateGames("basketball_nba", "us", LocalDate.now());
    }

    @Test
    void testFetchAndUpdateGamesFiltersInvalidGames() {
        final LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        final LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

        final OddsApiEvent validEvent = new OddsApiEvent("evt1", "basketball_nba", futureTime, "Lakers", "Celtics");
        final OddsApiEvent pastEvent = new OddsApiEvent("evt2", "basketball_nba", pastTime, "Warriors", "Nets");
        stubApiGateway.setEventsToReturn(List.of(validEvent, pastEvent));

        final FetchGamesOutputBoundary successPresenter = new FetchGamesOutputBoundary() {
            @Override
            public void presentFetchInProgress() {
                // Expected
            }

            @Override
            public void presentFetchSuccess(final FetchGamesResponseModel responseModel) {
                assertEquals(2, responseModel.getGamesFetched());
                // Only one game should be saved (past game filtered out)
                assertEquals(1, responseModel.getGamesSaved());
            }

            @Override
            public void presentFetchError(final String errorMessage) {
                fail("Unexpected error: " + errorMessage);
            }

            @Override
            public void presentSearchResults(final List<Game> games, final String query) {
                fail("Unexpected search results");
            }
        };

        interactor = new FetchGamesInteractor(stubApiGateway, responseAdapter, stubRepository, successPresenter);
        interactor.fetchAndUpdateGames("basketball_nba", "us", LocalDate.now());
    }

    @Test
    void testRefreshGames() {
        final LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        final OddsApiEvent event = new OddsApiEvent("evt1", "basketball_nba", futureTime, "Lakers", "Celtics");
        stubApiGateway.setEventsToReturn(List.of(event));

        final FetchGamesOutputBoundary successPresenter = new FetchGamesOutputBoundary() {
            @Override
            public void presentFetchInProgress() {
                // Expected
            }

            @Override
            public void presentFetchSuccess(final FetchGamesResponseModel responseModel) {
                assertEquals("basketball_nba", responseModel.getSport());
            }

            @Override
            public void presentFetchError(final String errorMessage) {
                fail("Unexpected error: " + errorMessage);
            }

            @Override
            public void presentSearchResults(final List<Game> games, final String query) {
                fail("Unexpected search results");
            }
        };

        interactor = new FetchGamesInteractor(stubApiGateway, responseAdapter, stubRepository, successPresenter);
        interactor.refreshGames();
    }

    @Test
    void testSearchGamesSuccess() {
        final List<Game> mockGames = createMockGames();
        stubRepository.setGamesToReturn(mockGames);

        final FetchGamesOutputBoundary successPresenter = new FetchGamesOutputBoundary() {
            @Override
            public void presentFetchInProgress() {
                fail("Unexpected fetch in progress");
            }

            @Override
            public void presentFetchSuccess(final FetchGamesResponseModel responseModel) {
                fail("Unexpected success");
            }

            @Override
            public void presentFetchError(final String errorMessage) {
                fail("Unexpected error: " + errorMessage);
            }

            @Override
            public void presentSearchResults(final List<Game> games, final String query) {
                assertEquals(2, games.size());
                assertEquals("Lakers", query);
            }
        };

        interactor = new FetchGamesInteractor(stubApiGateway, responseAdapter, stubRepository, successPresenter);
        interactor.searchGames("Lakers");
    }

    @Test
    void testSearchGamesRepositoryException() {
        stubRepository.setShouldThrowException(true);

        final FetchGamesOutputBoundary failPresenter = new FetchGamesOutputBoundary() {
            @Override
            public void presentFetchInProgress() {
                fail("Unexpected fetch in progress");
            }

            @Override
            public void presentFetchSuccess(final FetchGamesResponseModel responseModel) {
                fail("Unexpected success");
            }

            @Override
            public void presentFetchError(final String errorMessage) {
                assertEquals("Search failed: Repository failure", errorMessage);
            }

            @Override
            public void presentSearchResults(final List<Game> games, final String query) {
                fail("Unexpected search results");
            }
        };

        interactor = new FetchGamesInteractor(stubApiGateway, responseAdapter, stubRepository, failPresenter);
        interactor.searchGames("Lakers");
    }

    private List<Game> createMockGames() {
        final List<Game> games = new ArrayList<>();
        final LocalDateTime futureTime = LocalDateTime.now().plusDays(1);

        games.add(new Game(
            UUID.randomUUID(),
            UUID.randomUUID(),
            futureTime,
            "Lakers",
            "Celtics",
            "basketball_nba",
            GameStatus.UPCOMING,
            "evt1"
        ));

        games.add(new Game(
            UUID.randomUUID(),
            UUID.randomUUID(),
            futureTime.plusHours(3),
            "Warriors",
            "Nets",
            "basketball_nba",
            GameStatus.UPCOMING,
            "evt2"
        ));

        return games;
    }

    /**
     * Stub implementation of OddsApiGateway for testing.
     */
    private static final class StubOddsApiGateway implements OddsApiGateway {
        private List<OddsApiEvent> eventsToReturn = new ArrayList<>();
        private boolean shouldThrowException = false;

        void setEventsToReturn(final List<OddsApiEvent> events) {
            this.eventsToReturn = events;
        }

        void setShouldThrowException(final boolean shouldThrow) {
            this.shouldThrowException = shouldThrow;
        }

        @Override
        public List<OddsApiSport> fetchSports() throws ApiException {
            return new ArrayList<>();
        }

        @Override
        public List<OddsApiEvent> fetchEvents(final String sport, final String region, final LocalDate dateFrom)
                throws ApiException {
            if (shouldThrowException) {
                throw new ApiException("API failure");
            }
            return eventsToReturn;
        }
    }

    /**
     * Stub implementation of GameRepository for testing.
     */
    private static final class StubGameRepository implements GameRepository {
        private List<Game> gamesToReturn = new ArrayList<>();
        private boolean shouldThrowException = false;

        void setGamesToReturn(final List<Game> games) {
            this.gamesToReturn = games;
        }

        void setShouldThrowException(final boolean shouldThrow) {
            this.shouldThrowException = shouldThrow;
        }

        @Override
        public void upsertGames(final List<Game> games) throws RepositoryException {
            if (shouldThrowException) {
                throw new RepositoryException("Repository failure");
            }
            // No-op for testing
        }

        @Override
        public Optional<Game> findByExternalId(final String externalId) throws RepositoryException {
            if (shouldThrowException) {
                throw new RepositoryException("Repository failure");
            }
            return Optional.empty();
        }

        @Override
        public List<Game> findFutureGames() throws RepositoryException {
            if (shouldThrowException) {
                throw new RepositoryException("Repository failure");
            }
            return gamesToReturn;
        }

        @Override
        public List<Game> searchGames(final String query) throws RepositoryException {
            if (shouldThrowException) {
                throw new RepositoryException("Repository failure");
            }
            return gamesToReturn;
        }
    }
}
