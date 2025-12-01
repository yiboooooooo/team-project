package stakemate.use_case.settle_market;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import stakemate.entity.Side;
import stakemate.entity.User;

// -@cs[ClassDataAbstractionCoupling] Tests require many dependencies for mocking/faking.
class SettleMarketInteractorTest {

    private FakeBetRepository betRepository;
    private FakeAccountRepository accountRepository;
    private FakeSettlementRecordRepository settlementRecordRepository;
    private SpyPresenter presenter;
    private SettleMarketInteractor interactor;

    @BeforeEach
    void setUp() {
        betRepository = new FakeBetRepository();
        accountRepository = new FakeAccountRepository();
        settlementRecordRepository = new FakeSettlementRecordRepository();
        presenter = new SpyPresenter();
        interactor = new SettleMarketInteractor(
            betRepository,
            accountRepository,
            settlementRecordRepository,
            presenter
        );
    }

    @Test
    void testExecuteNullBetsReturnsFailure() {
        // Covers: if (bets == null)
        betRepository.setReturnNull(true);
        final SettleMarketRequestModel request = new SettleMarketRequestModel("m1", true);

        interactor.execute(request);

        assertTrue(presenter.failureCalled);
        assertEquals("No bets found for this market ", presenter.failureMessage);
    }

    @Test
    void testExecuteEmptyBetsReturnsFailure() {
        // Covers: if (bets.isEmpty())
        // Fake repo defaults to empty list
        final SettleMarketRequestModel request = new SettleMarketRequestModel("m1", true);

        interactor.execute(request);

        assertTrue(presenter.failureCalled);
        assertEquals("No bets found for this market ", presenter.failureMessage);
    }

    @Test
    void testExecuteUserNotFoundSkipsBet() {
        // Covers: if (user == null) continue;
        final String marketId = "m_ghost";

        // Create a bet for a user "ghost" who is NOT in the accountRepository
        final Bet ghostBet = new Bet("ghost", marketId, Side.BUY, 100.0, 0.5, null, false);

        final List<Bet> bets = new ArrayList<>();
        bets.add(ghostBet);
        betRepository.setBets(marketId, bets);

        final SettleMarketRequestModel request = new SettleMarketRequestModel(marketId, true);
        interactor.execute(request);

        assertTrue(presenter.successCalled);
        // Should be 0 because the user was null, so the loop 'continued' before incrementing count
        assertEquals(0, presenter.successModel.getBetsSettled());
        // No bets saved because we skipped
        assertEquals(0, betRepository.savedBets.size());
    }

    @Test
    void testExecuteHomeTeamWins() {
        // Covers branches:
        // 1. BUY && homeWon -> True (Winner)
        // 2. SELL && !homeWon -> False (Loser)
        // 3. calculatePayout -> if (won) vs else
        // 4. if (payout > 0) -> True (for Winner) vs False (for Loser)

        final String marketId = "m_home";
        final User alice = new User("alice", "pass", 1000);
        final User bob = new User("bob", "pass", 1000);
        accountRepository.save(alice);
        accountRepository.save(bob);

        // Alice BUY (Wins): Stake 100, Price 0.4
        // Payout Calculation: 100 * (1 - 0.4) + 0.4 * 100 = 60 + 40 = 100.
        // Net Result: 100 (Payout) - (100 * 0.4 Cost) = 60 Profit.
        final Bet betAlice = new Bet("alice", marketId, Side.BUY, 100.0, 0.4, null, false);

        // Bob SELL (Loses): Stake 100, Price 0.6
        // Payout Calculation: 0.
        // Net Result: -(100 * 0.6 Cost) = -60 Loss.
        final Bet betBob = new Bet("bob", marketId, Side.SELL, 100.0, 0.6, null, false);

        final List<Bet> bets = new ArrayList<>();
        bets.add(betAlice);
        bets.add(betBob);
        betRepository.setBets(marketId, bets);

        // Act: Home Wins = TRUE
        interactor.execute(new SettleMarketRequestModel(marketId, true));

        assertTrue(presenter.successCalled);
        final SettleMarketResponseModel response = presenter.successModel;

        // 1. Check Total Payout (Only Alice got paid 100)
        assertEquals(100.0, response.getTotalPayout(), 0.001);

        // 2. Check Balances
        // Alice: 1000 + 100 = 1100
        assertEquals(1100, accountRepository.findByUsername("alice").getBalance());
        // Bob: 1000 + 0 = 1000 (No change on loss in this logic)
        assertEquals(1000, accountRepository.findByUsername("bob").getBalance());

        // 3. Check Saved Bet Status
        assertTrue(betRepository.savedBets.get(0).isWon());
        assertFalse(betRepository.savedBets.get(1).isWon());

        // 4. Check Summary String for Net Result
        final String summary = response.getSettlementSummary();
        // Alice: Payout(100) - Cost(40) = 60
        assertTrue(summary.contains("alice: WON ($60.00)"));
        // Bob: -Cost(60) = -60
        assertTrue(summary.contains("bob: LOST ($-60.00)"));
    }

    @Test
    void testExecuteAwayTeamWins() {
        // Covers branches:
        // 1. BUY && homeWon -> False (Loser)
        // 2. SELL && !homeWon -> True (Winner)

        final String marketId = "m_away";
        final User alice = new User("alice", "pass", 1000);
        final User bob = new User("bob", "pass", 1000);
        accountRepository.save(alice);
        accountRepository.save(bob);

        // Alice BUY (Loses because Away won)
        final Bet betAlice = new Bet("alice", marketId, Side.BUY, 100.0, 0.4, null, false);

        // Bob SELL (Wins because Away won)
        final Bet betBob = new Bet("bob", marketId, Side.SELL, 100.0, 0.6, null, false);

        final List<Bet> bets = new ArrayList<>();
        bets.add(betAlice);
        bets.add(betBob);
        betRepository.setBets(marketId, bets);

        // Act: Home Wins = FALSE
        interactor.execute(new SettleMarketRequestModel(marketId, false));

        assertTrue(presenter.successCalled);

        // Verify Alice (Index 0) is LOST
        assertFalse(betRepository.savedBets.get(0).isWon());
        // Verify Bob (Index 1) is WON
        assertTrue(betRepository.savedBets.get(1).isWon());

        // Verify Summary
        final String summary = presenter.successModel.getSettlementSummary();
        // Alice: -Cost(40) = -40
        assertTrue(summary.contains("alice: LOST ($-40.00)"));
        // Bob: Payout(100) - Cost(60) = 40
        assertTrue(summary.contains("bob: WON ($40.00)"));
    }

    // =========================================================================
    // Fakes & Spies
    // =========================================================================

    private static final class FakeBetRepository implements BetRepository {
        private boolean returnNull;
        private List<Bet> bets = new ArrayList<>();
        private final List<Bet> savedBets = new ArrayList<>();

        void setReturnNull(final boolean val) {
            this.returnNull = val;
        }

        void setBets(final String marketId, final List<Bet> newBets) {
            this.bets = newBets;
        }

        @Override
        public List<Bet> findByMarketId(final String marketId) {
            final List<Bet> result;
            if (returnNull) {
                result = null;
            }
            else {
                result = bets;
            }
            return result;
        }

        @Override
        public void save(final Bet bet) {
            savedBets.add(bet);
        }
    }

    private static final class FakeAccountRepository implements AccountRepository {
        private final List<User> users = new ArrayList<>();

        @Override
        public User findByUsername(final String username) {
            return users.stream()
                .filter(userObj -> userObj.getUsername().equals(username))
                .findFirst()
                .orElse(null);
        }

        @Override
        public void save(final User user) {
            users.removeIf(userObj -> userObj.getUsername().equals(user.getUsername()));
            users.add(user);
        }
    }

    private static final class FakeSettlementRecordRepository implements SettlementRecordRepository {
        private final List<SettlementRecord> savedRecords = new ArrayList<>();

        @Override
        public Optional<SettlementRecord> findByMarketId(final String marketId) {
            return Optional.empty();
        }

        @Override
        public void save(final SettlementRecord record) {
            savedRecords.add(record);
        }
    }

    private static final class SpyPresenter implements SettleMarketOutputBoundary {
        private boolean successCalled;
        private boolean failureCalled;
        private SettleMarketResponseModel successModel;
        private String failureMessage;

        @Override
        public void presentSuccess(final SettleMarketResponseModel responseModel) {
            successCalled = true;
            successModel = responseModel;
        }

        @Override
        public void presentFailure(final String errorMessage) {
            failureCalled = true;
            failureMessage = errorMessage;
        }
    }
}
