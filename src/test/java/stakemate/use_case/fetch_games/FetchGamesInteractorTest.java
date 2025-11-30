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

    @Test
    void testFetchAndUpdateGamesUnexpectedException() {
        // Create a special adapter that throws a RuntimeException
        final OddsApiResponseAdapter faultyAdapter = new OddsApiResponseAdapter() {
            @Override
            public List<Game> convertToGames(final List<OddsApiEvent> events) {
                throw new RuntimeException("Unexpected conversion error");
            }
        };

        final LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        final OddsApiEvent event = new OddsApiEvent("evt1", "basketball_nba", futureTime, "Lakers", "Celtics");
        stubApiGateway.setEventsToReturn(List.of(event));

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
                assertEquals("Unexpected error: Unexpected conversion error", errorMessage);
            }

            @Override
            public void presentSearchResults(final List<Game> games, final String query) {
                fail("Unexpected search results");
            }
        };

        interactor = new FetchGamesInteractor(stubApiGateway, faultyAdapter, stubRepository, failPresenter);
        interactor.fetchAndUpdateGames("basketball_nba", "us", LocalDate.now());
    }

    @Test
    void testFetchAndUpdateGamesWithNullDateFrom() {
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
        interactor.fetchAndUpdateGames("basketball_nba", "us", null);
    }

    @Test
    void testFetchAndUpdateGamesNoValidGamesAfterConversion() {
        // Create an adapter that returns games with invalid data
        final OddsApiResponseAdapter invalidAdapter = new OddsApiResponseAdapter() {
            @Override
            public List<Game> convertToGames(final List<OddsApiEvent> events) {
                // Return empty list simulating conversion failure
                return new ArrayList<>();
            }
        };

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
                assertEquals(1, responseModel.getGamesFetched());
                assertEquals(0, responseModel.getGamesSaved());
                assertEquals("Fetched events but none could be converted to games.", responseModel.getMessage());
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

        interactor = new FetchGamesInteractor(stubApiGateway, invalidAdapter, stubRepository, successPresenter);
        interactor.fetchAndUpdateGames("basketball_nba", "us", LocalDate.now());
    }

    @Test
    void testFetchAndUpdateGamesFiltersInvalidGameWithNullId() {
        final OddsApiResponseAdapter adapterWithNullId = new OddsApiResponseAdapter() {
            @Override
            public List<Game> convertToGames(final List<OddsApiEvent> events) {
                final LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
                return List.of(new Game(null, UUID.randomUUID(), futureTime, "Lakers", "Celtics",
                        "basketball_nba", GameStatus.UPCOMING, "evt1"));
            }
        };

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
                assertEquals(1, responseModel.getGamesFetched());
                assertEquals(0, responseModel.getGamesSaved());
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

        interactor = new FetchGamesInteractor(stubApiGateway, adapterWithNullId, stubRepository, successPresenter);
        interactor.fetchAndUpdateGames("basketball_nba", "us", LocalDate.now());
    }

    @Test
    void testFetchAndUpdateGamesFiltersInvalidGameWithNullMarketId() {
        final OddsApiResponseAdapter adapterWithNullMarketId = new OddsApiResponseAdapter() {
            @Override
            public List<Game> convertToGames(final List<OddsApiEvent> events) {
                final LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
                return List.of(new Game(UUID.randomUUID(), null, futureTime, "Lakers", "Celtics",
                        "basketball_nba", GameStatus.UPCOMING, "evt1"));
            }
        };

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
                assertEquals(1, responseModel.getGamesFetched());
                assertEquals(0, responseModel.getGamesSaved());
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

        interactor = new FetchGamesInteractor(stubApiGateway, adapterWithNullMarketId, stubRepository, successPresenter);
        interactor.fetchAndUpdateGames("basketball_nba", "us", LocalDate.now());
    }

    @Test
    void testFetchAndUpdateGamesFiltersInvalidGameWithNullGameTime() {
        final OddsApiResponseAdapter adapterWithNullGameTime = new OddsApiResponseAdapter() {
            @Override
            public List<Game> convertToGames(final List<OddsApiEvent> events) {
                return List.of(new Game(UUID.randomUUID(), UUID.randomUUID(), null, "Lakers", "Celtics",
                        "basketball_nba", GameStatus.UPCOMING, "evt1"));
            }
        };

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
                assertEquals(1, responseModel.getGamesFetched());
                assertEquals(0, responseModel.getGamesSaved());
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

        interactor = new FetchGamesInteractor(stubApiGateway, adapterWithNullGameTime, stubRepository, successPresenter);
        interactor.fetchAndUpdateGames("basketball_nba", "us", LocalDate.now());
    }

    @Test
    void testFetchAndUpdateGamesFiltersInvalidGameWithNullTeamA() {
        final OddsApiResponseAdapter adapterWithNullTeamA = new OddsApiResponseAdapter() {
            @Override
            public List<Game> convertToGames(final List<OddsApiEvent> events) {
                final LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
                return List.of(new Game(UUID.randomUUID(), UUID.randomUUID(), futureTime, null, "Celtics",
                        "basketball_nba", GameStatus.UPCOMING, "evt1"));
            }
        };

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
                assertEquals(1, responseModel.getGamesFetched());
                assertEquals(0, responseModel.getGamesSaved());
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

        interactor = new FetchGamesInteractor(stubApiGateway, adapterWithNullTeamA, stubRepository, successPresenter);
        interactor.fetchAndUpdateGames("basketball_nba", "us", LocalDate.now());
    }

    @Test
    void testFetchAndUpdateGamesFiltersInvalidGameWithEmptyTeamA() {
        final OddsApiResponseAdapter adapterWithEmptyTeamA = new OddsApiResponseAdapter() {
            @Override
            public List<Game> convertToGames(final List<OddsApiEvent> events) {
                final LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
                return List.of(new Game(UUID.randomUUID(), UUID.randomUUID(), futureTime, "  ", "Celtics",
                        "basketball_nba", GameStatus.UPCOMING, "evt1"));
            }
        };

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
                assertEquals(1, responseModel.getGamesFetched());
                assertEquals(0, responseModel.getGamesSaved());
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

        interactor = new FetchGamesInteractor(stubApiGateway, adapterWithEmptyTeamA, stubRepository, successPresenter);
        interactor.fetchAndUpdateGames("basketball_nba", "us", LocalDate.now());
    }

    @Test
    void testFetchAndUpdateGamesFiltersInvalidGameWithNullTeamB() {
        final OddsApiResponseAdapter adapterWithNullTeamB = new OddsApiResponseAdapter() {
            @Override
            public List<Game> convertToGames(final List<OddsApiEvent> events) {
                final LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
                return List.of(new Game(UUID.randomUUID(), UUID.randomUUID(), futureTime, "Lakers", null,
                        "basketball_nba", GameStatus.UPCOMING, "evt1"));
            }
        };

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
                assertEquals(1, responseModel.getGamesFetched());
                assertEquals(0, responseModel.getGamesSaved());
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

        interactor = new FetchGamesInteractor(stubApiGateway, adapterWithNullTeamB, stubRepository, successPresenter);
        interactor.fetchAndUpdateGames("basketball_nba", "us", LocalDate.now());
    }

    @Test
    void testFetchAndUpdateGamesFiltersInvalidGameWithEmptyTeamB() {
        final OddsApiResponseAdapter adapterWithEmptyTeamB = new OddsApiResponseAdapter() {
            @Override
            public List<Game> convertToGames(final List<OddsApiEvent> events) {
                final LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
                return List.of(new Game(UUID.randomUUID(), UUID.randomUUID(), futureTime, "Lakers", "  ",
                        "basketball_nba", GameStatus.UPCOMING, "evt1"));
            }
        };

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
                assertEquals(1, responseModel.getGamesFetched());
                assertEquals(0, responseModel.getGamesSaved());
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

        interactor = new FetchGamesInteractor(stubApiGateway, adapterWithEmptyTeamB, stubRepository, successPresenter);
        interactor.fetchAndUpdateGames("basketball_nba", "us", LocalDate.now());
    }

    @Test
    void testFetchAndUpdateGamesFiltersInvalidGameWithNullSport() {
        final OddsApiResponseAdapter adapterWithNullSport = new OddsApiResponseAdapter() {
            @Override
            public List<Game> convertToGames(final List<OddsApiEvent> events) {
                final LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
                return List.of(new Game(UUID.randomUUID(), UUID.randomUUID(), futureTime, "Lakers", "Celtics",
                        null, GameStatus.UPCOMING, "evt1"));
            }
        };

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
                assertEquals(1, responseModel.getGamesFetched());
                assertEquals(0, responseModel.getGamesSaved());
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

        interactor = new FetchGamesInteractor(stubApiGateway, adapterWithNullSport, stubRepository, successPresenter);
        interactor.fetchAndUpdateGames("basketball_nba", "us", LocalDate.now());
    }

    @Test
    void testFetchAndUpdateGamesFiltersInvalidGameWithEmptySport() {
        final OddsApiResponseAdapter adapterWithEmptySport = new OddsApiResponseAdapter() {
            @Override
            public List<Game> convertToGames(final List<OddsApiEvent> events) {
                final LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
                return List.of(new Game(UUID.randomUUID(), UUID.randomUUID(), futureTime, "Lakers", "Celtics",
                        "   ", GameStatus.UPCOMING, "evt1"));
            }
        };

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
                assertEquals(1, responseModel.getGamesFetched());
                assertEquals(0, responseModel.getGamesSaved());
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

        interactor = new FetchGamesInteractor(stubApiGateway, adapterWithEmptySport, stubRepository, successPresenter);
        interactor.fetchAndUpdateGames("basketball_nba", "us", LocalDate.now());
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
