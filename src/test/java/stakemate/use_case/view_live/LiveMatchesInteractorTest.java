package stakemate.use_case.view_live;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import stakemate.entity.Game;
import stakemate.entity.GameStatus;
import stakemate.use_case.fetch_games.FetchGamesInputBoundary;
import stakemate.use_case.fetch_games.GameRepository;
import stakemate.use_case.fetch_games.RepositoryException;

/**
 * Tests for the LiveMatchesInteractor.
 */
class LiveMatchesInteractorTest {

    private StubFetchGamesInteractor stubFetchGamesInteractor;
    private StubGameRepository stubGameRepository;
    private LiveMatchesInteractor interactor;

    @BeforeEach
    void setUp() {
        stubFetchGamesInteractor = new StubFetchGamesInteractor();
        stubGameRepository = new StubGameRepository();
    }

    @Test
    void testStartTrackingSuccess() throws InterruptedException {
        final List<Game> mockGames = createMockGames();
        stubGameRepository.setGamesToReturn(mockGames);

        final TestLiveMatchesPresenter presenter = new TestLiveMatchesPresenter();
        interactor = new LiveMatchesInteractor(stubFetchGamesInteractor, stubGameRepository, presenter);

        interactor.startTracking();

        // Wait a bit for the first scheduled execution
        Thread.sleep(100);

        // Verify refresh was called
        assertEquals(true, stubFetchGamesInteractor.wasRefreshCalled());

        // Verify presenter received matches
        assertEquals(true, presenter.wasMatchesCalled());
        assertEquals(2, presenter.getLastMatches().size());

        interactor.stopTracking();
    }

    @Test
    void testStartTrackingDoesNotStartIfAlreadyRunning() throws InterruptedException {
        final List<Game> mockGames = createMockGames();
        stubGameRepository.setGamesToReturn(mockGames);

        final TestLiveMatchesPresenter presenter = new TestLiveMatchesPresenter();
        interactor = new LiveMatchesInteractor(stubFetchGamesInteractor, stubGameRepository, presenter);

        // Start tracking
        interactor.startTracking();
        Thread.sleep(50);

        final int firstCallCount = stubFetchGamesInteractor.getRefreshCallCount();

        // Try to start again
        interactor.startTracking();
        Thread.sleep(50);

        // Should not increase call count significantly (maybe +1 from background thread)
        final int secondCallCount = stubFetchGamesInteractor.getRefreshCallCount();
        assertEquals(true, secondCallCount <= firstCallCount + 2);

        interactor.stopTracking();
    }

    @Test
    void testStopTrackingShutdownsScheduler() throws InterruptedException {
        final List<Game> mockGames = createMockGames();
        stubGameRepository.setGamesToReturn(mockGames);

        final TestLiveMatchesPresenter presenter = new TestLiveMatchesPresenter();
        interactor = new LiveMatchesInteractor(stubFetchGamesInteractor, stubGameRepository, presenter);

        interactor.startTracking();
        Thread.sleep(50);

        final int callCountBeforeStop = stubFetchGamesInteractor.getRefreshCallCount();

        // Stop tracking
        interactor.stopTracking();
        Thread.sleep(100);

        // Call count should not increase after stopping
        final int callCountAfterStop = stubFetchGamesInteractor.getRefreshCallCount();
        assertEquals(callCountBeforeStop, callCountAfterStop);
    }

    @Test
    void testFetchAndPresentHandlesRepositoryException() throws InterruptedException {
        stubGameRepository.setShouldThrowException(true);

        final TestLiveMatchesPresenter presenter = new TestLiveMatchesPresenter();
        interactor = new LiveMatchesInteractor(stubFetchGamesInteractor, stubGameRepository, presenter);

        interactor.startTracking();
        Thread.sleep(100);

        // Verify error was presented
        assertEquals(true, presenter.wasErrorCalled());
        assertEquals(true, presenter.getLastError().contains("Failed to retrieve matches"));

        interactor.stopTracking();
    }

    @Test
    void testFetchAndPresentHandlesRuntimeException() throws InterruptedException {
        stubGameRepository.setShouldThrowRuntimeException(true);

        final TestLiveMatchesPresenter presenter = new TestLiveMatchesPresenter();
        interactor = new LiveMatchesInteractor(stubFetchGamesInteractor, stubGameRepository, presenter);

        interactor.startTracking();
        Thread.sleep(100);

        // Verify error was presented
        assertEquals(true, presenter.wasErrorCalled());
        assertEquals(true, presenter.getLastError().contains("Unexpected error during tracking"));

        interactor.stopTracking();
    }

    @Test
    void testFetchAndPresentCallsRefreshGames() throws InterruptedException {
        final List<Game> mockGames = createMockGames();
        stubGameRepository.setGamesToReturn(mockGames);

        final TestLiveMatchesPresenter presenter = new TestLiveMatchesPresenter();
        interactor = new LiveMatchesInteractor(stubFetchGamesInteractor, stubGameRepository, presenter);

        interactor.startTracking();
        Thread.sleep(100);

        // Verify fetchGamesInteractor.refreshGames() was called
        assertEquals(true, stubFetchGamesInteractor.wasRefreshCalled());
        assertEquals(true, stubFetchGamesInteractor.getRefreshCallCount() > 0);

        interactor.stopTracking();
    }

    @Test
    void testFetchAndPresentQueriesRepository() throws InterruptedException {
        final List<Game> mockGames = createMockGames();
        stubGameRepository.setGamesToReturn(mockGames);

        final TestLiveMatchesPresenter presenter = new TestLiveMatchesPresenter();
        interactor = new LiveMatchesInteractor(stubFetchGamesInteractor, stubGameRepository, presenter);

        interactor.startTracking();
        Thread.sleep(100);

        // Verify repository was queried
        assertEquals(true, stubGameRepository.wasFindFutureGamesCalled());

        interactor.stopTracking();
    }

    @Test
    void testFetchAndPresentPresentsGames() throws InterruptedException {
        final List<Game> mockGames = createMockGames();
        stubGameRepository.setGamesToReturn(mockGames);

        final TestLiveMatchesPresenter presenter = new TestLiveMatchesPresenter();
        interactor = new LiveMatchesInteractor(stubFetchGamesInteractor, stubGameRepository, presenter);

        interactor.startTracking();
        Thread.sleep(100);

        // Verify presenter.presentMatches() was called with correct data
        assertEquals(true, presenter.wasMatchesCalled());
        assertEquals(2, presenter.getLastMatches().size());
        assertEquals("Lakers", presenter.getLastMatches().get(0).getTeamA());

        interactor.stopTracking();
    }

    @Test
    void testStopTrackingWhenNotRunning() {
        // Should not throw exception
        final TestLiveMatchesPresenter presenter = new TestLiveMatchesPresenter();
        interactor = new LiveMatchesInteractor(stubFetchGamesInteractor, stubGameRepository, presenter);

        interactor.stopTracking();
        // No exception = success
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
     * Stub implementation of FetchGamesInputBoundary for testing.
     */
    private static final class StubFetchGamesInteractor implements FetchGamesInputBoundary {
        private boolean refreshCalled = false;
        private int refreshCallCount = 0;

        @Override
        public void fetchAndUpdateGames(final String sport, final String region,
                                        final java.time.LocalDate dateFrom) {
            // No-op for testing
        }

        @Override
        public void refreshGames() {
            refreshCalled = true;
            refreshCallCount++;
        }

        @Override
        public void searchGames(final String query) {
            // No-op for testing
        }

        boolean wasRefreshCalled() {
            return refreshCalled;
        }

        int getRefreshCallCount() {
            return refreshCallCount;
        }
    }

    /**
     * Stub implementation of GameRepository for testing.
     */
    private static final class StubGameRepository implements GameRepository {
        private List<Game> gamesToReturn = new ArrayList<>();
        private boolean shouldThrowException = false;
        private boolean shouldThrowRuntimeException = false;
        private boolean findFutureGamesCalled = false;

        void setGamesToReturn(final List<Game> games) {
            this.gamesToReturn = games;
        }

        void setShouldThrowException(final boolean shouldThrow) {
            this.shouldThrowException = shouldThrow;
        }

        void setShouldThrowRuntimeException(final boolean shouldThrow) {
            this.shouldThrowRuntimeException = shouldThrow;
        }

        boolean wasFindFutureGamesCalled() {
            return findFutureGamesCalled;
        }

        @Override
        public void upsertGames(final List<Game> games) throws RepositoryException {
            // No-op for testing
        }

        @Override
        public java.util.Optional<Game> findByExternalId(final String externalId) throws RepositoryException {
            return java.util.Optional.empty();
        }

        @Override
        public List<Game> findFutureGames() throws RepositoryException {
            findFutureGamesCalled = true;
            if (shouldThrowException) {
                throw new RepositoryException("Repository failure");
            }
            if (shouldThrowRuntimeException) {
                throw new RuntimeException("Unexpected runtime error");
            }
            return gamesToReturn;
        }

        @Override
        public List<Game> searchGames(final String query) throws RepositoryException {
            return gamesToReturn;
        }
    }

    /**
     * Test implementation of LiveMatchesOutputBoundary.
     */
    private static final class TestLiveMatchesPresenter implements LiveMatchesOutputBoundary {
        private boolean matchesCalled = false;
        private boolean errorCalled = false;
        private List<Game> lastMatches = null;
        private String lastError = null;

        @Override
        public void presentMatches(final List<Game> matches) {
            matchesCalled = true;
            lastMatches = matches;
        }

        @Override
        public void presentError(final String error) {
            errorCalled = true;
            lastError = error;
        }

        boolean wasMatchesCalled() {
            return matchesCalled;
        }

        boolean wasErrorCalled() {
            return errorCalled;
        }

        List<Game> getLastMatches() {
            return lastMatches;
        }

        String getLastError() {
            return lastError;
        }
    }
}
