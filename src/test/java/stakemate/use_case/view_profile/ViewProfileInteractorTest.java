package stakemate.use_case.view_profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import stakemate.entity.Side;
import stakemate.entity.User;
import stakemate.use_case.settle_market.Bet;
import stakemate.use_case.view_profile.strategy.DateBetComparator;
import stakemate.use_case.view_profile.strategy.SizeBetComparator;

/**
 * Tests for the View Profile Interactor.
 */
class ViewProfileInteractorTest {

    private StubUserDataAccess stubUserDataAccess;
    private ViewProfileInteractor interactor;

    @BeforeEach
    void setUp() {
        stubUserDataAccess = new StubUserDataAccess();
    }

    @Test
    void testUserNotFound() {
        stubUserDataAccess.setReturnNullUser(true);

        final ViewProfileOutputBoundary errorPresenter = new TestOutputBoundary() {
            @Override
            public void presentError(final String error) {
                assertEquals("User not found: nonExistentUser", error);
            }

            @Override
            public void presentProfile(final ViewProfileOutputData outputData) {
                fail("Unexpected success");
            }
        };

        interactor = new ViewProfileInteractor(stubUserDataAccess, errorPresenter);
        interactor.execute(new ViewProfileInputData("nonExistentUser"));
    }

    @Test
    void testUserFoundNoBets() {
        final User user = new User("testUser", "password", 10000);
        stubUserDataAccess.setUserToReturn(user);

        final ViewProfileOutputBoundary successPresenter = new TestOutputBoundary() {
            @Override
            public void presentProfile(final ViewProfileOutputData outputData) {
                assertEquals("testUser", outputData.getUsername());
                assertEquals(10000.0, outputData.getBalance());
                assertEquals(0.0, outputData.getPnl()); // 10000 - 10000
                assertTrue(outputData.getOpenPositions().isEmpty());
                assertTrue(outputData.getHistoricalPositions().isEmpty());
            }
        };

        interactor = new ViewProfileInteractor(stubUserDataAccess, successPresenter);
        interactor.execute(new ViewProfileInputData("testUser"));
    }

    @Test
    void testSeparationOfBets() {
        final User user = new User("testUser", "password", 10000);
        stubUserDataAccess.setUserToReturn(user);

        final Bet openBet = new Bet("testUser", "m1", Side.BUY, 100.0, 0.5, null, false);
        final Bet historicalBet = new Bet("testUser", "m2", Side.BUY, 100.0, 0.5, true, true);

        final List<Bet> bets = new ArrayList<>();
        bets.add(openBet);
        bets.add(historicalBet);
        stubUserDataAccess.setBetsToReturn(bets);

        final ViewProfileOutputBoundary successPresenter = new TestOutputBoundary() {
            @Override
            public void presentProfile(final ViewProfileOutputData outputData) {
                assertEquals(1, outputData.getOpenPositions().size());
                assertEquals(1, outputData.getHistoricalPositions().size());
                assertEquals("m1", outputData.getOpenPositions().get(0).getMarketId());
                assertEquals("m2", outputData.getHistoricalPositions().get(0).getMarketId());
            }
        };

        interactor = new ViewProfileInteractor(stubUserDataAccess, successPresenter);
        interactor.execute(new ViewProfileInputData("testUser"));
    }

    @Test
    void testSortingOpenBetsByDate() {
        final User user = new User("testUser", "password", 10000);
        stubUserDataAccess.setUserToReturn(user);

        final Instant now = Instant.now();
        // Bet 1: Older
        final Bet bet1 = new Bet("testUser", "m1", Side.BUY, 100.0, 0.5, null, false, null,
                now.minus(1, ChronoUnit.DAYS));
        // Bet 2: Newer
        final Bet bet2 = new Bet("testUser", "m2", Side.BUY, 100.0, 0.5, null, false, null, now);

        final List<Bet> bets = new ArrayList<>();
        bets.add(bet1);
        bets.add(bet2);
        stubUserDataAccess.setBetsToReturn(bets);

        final ViewProfileOutputBoundary successPresenter = new TestOutputBoundary() {
            @Override
            public void presentProfile(final ViewProfileOutputData outputData) {
                assertEquals("m2", outputData.getOpenPositions().get(0).getMarketId()); // Newer first
                assertEquals("m1", outputData.getOpenPositions().get(1).getMarketId());
            }
        };

        interactor = new ViewProfileInteractor(stubUserDataAccess, successPresenter);
        interactor.execute(new ViewProfileInputData("testUser", new DateBetComparator(), new DateBetComparator()));
    }

    @Test
    void testSortingOpenBetsBySize() {
        final User user = new User("testUser", "password", 10000);
        stubUserDataAccess.setUserToReturn(user);

        // Size = Stake * Price
        final Bet bet1 = new Bet("testUser", "m1", Side.BUY, 100.0, 0.5, null, false); // 50.0
        final Bet bet2 = new Bet("testUser", "m2", Side.BUY, 200.0, 0.5, null, false); // 100.0

        final List<Bet> bets = new ArrayList<>();
        bets.add(bet1);
        bets.add(bet2);
        stubUserDataAccess.setBetsToReturn(bets);

        final ViewProfileOutputBoundary successPresenter = new TestOutputBoundary() {
            @Override
            public void presentProfile(final ViewProfileOutputData outputData) {
                assertEquals("m2", outputData.getOpenPositions().get(0).getMarketId()); // Larger first
                assertEquals("m1", outputData.getOpenPositions().get(1).getMarketId());
            }
        };

        interactor = new ViewProfileInteractor(stubUserDataAccess, successPresenter);
        interactor.execute(new ViewProfileInputData("testUser", new SizeBetComparator(), new DateBetComparator()));
    }

    @Test
    void testSortingHistoricalBetsByDate() {
        final User user = new User("testUser", "password", 10000);
        stubUserDataAccess.setUserToReturn(user);

        final Instant now = Instant.now();
        // Bet 1: Older
        final Bet bet1 = new Bet("testUser", "m1", Side.BUY, 100.0, 0.5, true, true, null,
                now.minus(1, ChronoUnit.DAYS));
        // Bet 2: Newer
        final Bet bet2 = new Bet("testUser", "m2", Side.BUY, 100.0, 0.5, true, true, null, now);

        final List<Bet> bets = new ArrayList<>();
        bets.add(bet1);
        bets.add(bet2);
        stubUserDataAccess.setBetsToReturn(bets);

        final ViewProfileOutputBoundary successPresenter = new TestOutputBoundary() {
            @Override
            public void presentProfile(final ViewProfileOutputData outputData) {
                assertEquals("m2", outputData.getHistoricalPositions().get(0).getMarketId()); // Newer first
                assertEquals("m1", outputData.getHistoricalPositions().get(1).getMarketId());
            }
        };

        interactor = new ViewProfileInteractor(stubUserDataAccess, successPresenter);
        interactor.execute(new ViewProfileInputData("testUser", new DateBetComparator(), new DateBetComparator()));
    }

    @Test
    void testSortingHistoricalBetsBySize() {
        final User user = new User("testUser", "password", 10000);
        stubUserDataAccess.setUserToReturn(user);

        // Size = Stake * (Won ? 1 : 0)
        final Bet bet1 = new Bet("testUser", "m1", Side.BUY, 100.0, 0.5, true, true); // Won, val=100
        final Bet bet2 = new Bet("testUser", "m2", Side.BUY, 200.0, 0.5, false, true); // Lost, val=0
        final Bet bet3 = new Bet("testUser", "m3", Side.BUY, 150.0, 0.5, true, true); // Won, val=150

        final List<Bet> bets = new ArrayList<>();
        bets.add(bet1);
        bets.add(bet2);
        bets.add(bet3);
        stubUserDataAccess.setBetsToReturn(bets);

        final ViewProfileOutputBoundary successPresenter = new TestOutputBoundary() {
            @Override
            public void presentProfile(final ViewProfileOutputData outputData) {
                assertEquals("m3", outputData.getHistoricalPositions().get(0).getMarketId()); // 150
                assertEquals("m1", outputData.getHistoricalPositions().get(1).getMarketId()); // 100
                assertEquals("m2", outputData.getHistoricalPositions().get(2).getMarketId()); // 0
            }
        };

        interactor = new ViewProfileInteractor(stubUserDataAccess, successPresenter);
        interactor.execute(new ViewProfileInputData("testUser", new DateBetComparator(), new SizeBetComparator()));
    }

    @Test
    void testDataPassing() {
        final User user = new User("testUser", "password", 10000);
        stubUserDataAccess.setUserToReturn(user);

        final Instant now = Instant.now();

        // Open Bet with Team Name
        final Bet openBet = new Bet("testUser", "m1", Side.BUY, 100.0, 0.5, null, false, "TeamA", now);

        // Historical Won with Team Name (Older)
        final Bet histBetWon = new Bet("testUser", "m2", Side.BUY, 100.0, 0.5, true, true, "TeamB",
                now.minus(1, ChronoUnit.HOURS));

        // Historical Lost with Team Name (Newer)
        final Bet histBetLost = new Bet("testUser", "m3", Side.BUY, 100.0, 0.5, false, true, "TeamC", now);

        final List<Bet> bets = new ArrayList<>();
        bets.add(openBet);
        bets.add(histBetWon);
        bets.add(histBetLost);
        stubUserDataAccess.setBetsToReturn(bets);

        final ViewProfileOutputBoundary successPresenter = new TestOutputBoundary() {
            @Override
            public void presentProfile(final ViewProfileOutputData outputData) {
                // Verify Open Position Data
                final Bet open = outputData.getOpenPositions().get(0);
                assertEquals("m1", open.getMarketId());
                assertEquals("TeamA", open.getTeamName());
                assertEquals(0.5, open.getPrice());
                assertEquals(100.0, open.getStake());

                // Verify Historical Positions (Sorted by Date Descending)
                final Bet histLost = outputData.getHistoricalPositions().get(0);
                assertEquals("m3", histLost.getMarketId());
                assertEquals("TeamC", histLost.getTeamName());

                final Bet histWon = outputData.getHistoricalPositions().get(1);
                assertEquals("m2", histWon.getMarketId());
                assertEquals("TeamB", histWon.getTeamName());
            }
        };

        interactor = new ViewProfileInteractor(stubUserDataAccess, successPresenter);
        interactor.execute(new ViewProfileInputData("testUser", new DateBetComparator(), new DateBetComparator()));
    }

    @Test
    void testSortCriteriaNull() {
        final User user = new User("testUser", "password", 10000);
        stubUserDataAccess.setUserToReturn(user);

        final Bet bet1 = new Bet("testUser", "m1", Side.BUY, 100.0, 0.5, null, false);
        final Bet bet2 = new Bet("testUser", "m2", Side.BUY, 100.0, 0.5, null, false);

        final List<Bet> bets = new ArrayList<>();
        bets.add(bet1);
        bets.add(bet2);
        stubUserDataAccess.setBetsToReturn(bets);

        final ViewProfileOutputBoundary successPresenter = new TestOutputBoundary() {
            @Override
            public void presentProfile(final ViewProfileOutputData outputData) {
                // Order should be preserved (insertion order) or undefined but no crash
                assertEquals(2, outputData.getOpenPositions().size());
            }
        };

        interactor = new ViewProfileInteractor(stubUserDataAccess, successPresenter);
        // Pass null for sort criteria to hit the implicit else branches
        interactor.execute(new ViewProfileInputData("testUser", null, null));
    }

    // =========================================================================
    // Stubs
    // =========================================================================

    private static class StubUserDataAccess implements ViewProfileUserDataAccessInterface {
        private boolean returnNullUser = false;
        private User userToReturn;
        private List<Bet> betsToReturn = new ArrayList<>();

        void setReturnNullUser(final boolean returnNullUser) {
            this.returnNullUser = returnNullUser;
        }

        void setUserToReturn(final User user) {
            this.userToReturn = user;
        }

        void setBetsToReturn(final List<Bet> bets) {
            this.betsToReturn = bets;
        }

        @Override
        public User getByUsername(final String username) {
            if (returnNullUser) {
                return null;
            }
            return userToReturn;
        }

        @Override
        public List<Bet> getPositionsByUsername(final String username) {
            return betsToReturn;
        }
    }

    private static class TestOutputBoundary implements ViewProfileOutputBoundary {
        @Override
        public void presentProfile(final ViewProfileOutputData outputData) {
            fail("Unexpected call to presentProfile");
        }

        @Override
        public void presentError(final String error) {
            fail("Unexpected call to presentError: " + error);
        }
    }
}
