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
                assertEquals("m1", outputData.getOpenPositions().get(0)[0]);
                assertEquals("m2", outputData.getHistoricalPositions().get(0)[0]);
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
                assertEquals("m2", outputData.getOpenPositions().get(0)[0]); // Newer first
                assertEquals("m1", outputData.getOpenPositions().get(1)[0]);
            }
        };

        interactor = new ViewProfileInteractor(stubUserDataAccess, successPresenter);
        interactor.execute(new ViewProfileInputData("testUser", SortCriteria.DATE, SortCriteria.DATE));
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
                assertEquals("m2", outputData.getOpenPositions().get(0)[0]); // Larger first
                assertEquals("m1", outputData.getOpenPositions().get(1)[0]);
            }
        };

        interactor = new ViewProfileInteractor(stubUserDataAccess, successPresenter);
        interactor.execute(new ViewProfileInputData("testUser", SortCriteria.SIZE, SortCriteria.DATE));
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
                assertEquals("m2", outputData.getHistoricalPositions().get(0)[0]); // Newer first
                assertEquals("m1", outputData.getHistoricalPositions().get(1)[0]);
            }
        };

        interactor = new ViewProfileInteractor(stubUserDataAccess, successPresenter);
        interactor.execute(new ViewProfileInputData("testUser", SortCriteria.DATE, SortCriteria.DATE));
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
                assertEquals("m3", outputData.getHistoricalPositions().get(0)[0]); // 150
                assertEquals("m1", outputData.getHistoricalPositions().get(1)[0]); // 100
                assertEquals("m2", outputData.getHistoricalPositions().get(2)[0]); // 0
            }
        };

        interactor = new ViewProfileInteractor(stubUserDataAccess, successPresenter);
        interactor.execute(new ViewProfileInputData("testUser", SortCriteria.DATE, SortCriteria.SIZE));
    }

    @Test
    void testOutputFormatting() {
        final User user = new User("testUser", "password", 10000);
        stubUserDataAccess.setUserToReturn(user);

        // Open Bet with Team Name
        final Bet openBet = new Bet("testUser", "m1", Side.BUY, 100.0, 0.5, null, false, "TeamA");

        // Historical Won with Team Name
        final Bet histBetWon = new Bet("testUser", "m2", Side.BUY, 100.0, 0.5, true, true, "TeamB");

        // Historical Lost with Team Name
        final Bet histBetLost = new Bet("testUser", "m3", Side.BUY, 100.0, 0.5, false, true, "TeamC");

        final List<Bet> bets = new ArrayList<>();
        bets.add(openBet);
        bets.add(histBetWon);
        bets.add(histBetLost);
        stubUserDataAccess.setBetsToReturn(bets);

        final ViewProfileOutputBoundary successPresenter = new TestOutputBoundary() {
            @Override
            public void presentProfile(final ViewProfileOutputData outputData) {
                // Open Position: [marketName, team, buyPrice, size, buyAmt, potentialProfit]
                final String[] open = outputData.getOpenPositions().get(0);
                assertEquals("m1", open[0]);
                assertEquals("TeamA", open[1]);
                assertEquals("0.50", open[2]);
                assertEquals("100", open[3]);
                assertEquals("50.00", open[4]); // 100 * 0.5
                assertEquals("50.00", open[5]); // 100 * (1 - 0.5)

                // Historical Won: [marketName, team, buyPrice, size, profit]
                // Profit = (1 - 0.5) * 100 = 50.00
                final String[] histWon = outputData.getHistoricalPositions().get(0);
                assertEquals("m2", histWon[0]);
                assertEquals("50.00", histWon[4]);

                // Historical Lost: [marketName, team, buyPrice, size, profit]
                // Profit = -1 * 0.5 * 100 = -50.00
                final String[] histLost = outputData.getHistoricalPositions().get(1);
                assertEquals("m3", histLost[0]);
                assertEquals("-50.00", histLost[4]);
            }
        };

        interactor = new ViewProfileInteractor(stubUserDataAccess, successPresenter);
        interactor.execute(new ViewProfileInputData("testUser", SortCriteria.DATE, SortCriteria.DATE));
    }

    // =========================================================================
    // Stubs
    // =========================================================================

    private static class StubUserDataAccess implements ViewProfileUserDataAccessInterface {
        private boolean returnNullUser = false;
        private User userToReturn;
        private List<Bet> betsToReturn = new ArrayList<>();

        void setReturnNullUser(boolean returnNullUser) {
            this.returnNullUser = returnNullUser;
        }

        void setUserToReturn(User user) {
            this.userToReturn = user;
        }

        void setBetsToReturn(List<Bet> bets) {
            this.betsToReturn = bets;
        }

        @Override
        public User getByUsername(String username) {
            if (returnNullUser) {
                return null;
            }
            return userToReturn;
        }

        @Override
        public List<Bet> getPositionsByUsername(String username) {
            return betsToReturn;
        }
    }

    private static class TestOutputBoundary implements ViewProfileOutputBoundary {
        @Override
        public void presentProfile(ViewProfileOutputData outputData) {
            fail("Unexpected call to presentProfile");
        }

        @Override
        public void presentError(String error) {
            fail("Unexpected call to presentError: " + error);
        }
    }
}
