package stakemate.use_case.settle_market;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stakemate.entity.Side;
import stakemate.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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
    void testExecute_NullBets_ReturnsFailure() {
        // Arrange
        betRepository.setReturnNull(true);
        SettleMarketRequestModel request = new SettleMarketRequestModel("m1");

        // Act
        interactor.execute(request);

        // Assert
        assertTrue(presenter.failureCalled);
        assertEquals("No bets found for market m1", presenter.failureMessage);
        assertFalse(presenter.successCalled);
    }

    @Test
    void testExecute_EmptyBets_ReturnsFailure() {
        // Arrange (default empty list in fake)
        SettleMarketRequestModel request = new SettleMarketRequestModel("m1");

        // Act
        interactor.execute(request);

        // Assert
        assertTrue(presenter.failureCalled);
        assertEquals("No bets found for market m1", presenter.failureMessage);
        assertFalse(presenter.successCalled);
    }

    @Test
    void testExecute_Settlement_WonAndLostAndUserMissing() {
        // Arrange
        String marketId = "market_123";

        // Setup Users
        // 1. Winner: starts with 1000
        User winner = new User("winner", "pass", 1000);
        // 2. Loser: starts with 1000
        User loser = new User("loser", "pass", 1000);
        // 3. Null Flag Loser: starts with 1000
        User nullFlagLoser = new User("nullUser", "pass", 1000);

        // Save users to fake repo
        accountRepository.save(winner);
        accountRepository.save(loser);
        accountRepository.save(nullFlagLoser);
        // Note: "ghost" user is NOT saved to repo to test user==null branch

        // Setup Bets
        // Bet 1: Winner. Won=true. Stake 100, Price 0.4.
        // Logic: Profit = 100 * (1 - 0.4) = 60. New Balance = 1060.
        Bet bet1 = new Bet("winner", marketId, Side.BUY, 100.0, 0.4, true, false);

        // Bet 2: Loser. Won=false. Stake 50, Price 0.5.
        // Logic: Payout = -50 * 0.5 = -25. New Balance = 975.
        Bet bet2 = new Bet("loser", marketId, Side.BUY, 50.0, 0.5, false, false);

        // Bet 3: Null won flag (counts as lost). Won=null. Stake 100, Price 0.2.
        // Logic: Payout = -100 * 0.2 = -20. New Balance = 980.
        Bet bet3 = new Bet("nullUser", marketId, Side.BUY, 100.0, 0.2, null, false);

        // Bet 4: User missing. Won=true (doesn't matter). Stake 10.0.
        // Logic: Should trigger warning and continue loop without saving.
        Bet bet4 = new Bet("ghost", marketId, Side.BUY, 10.0, 0.5, true, false);

        List<Bet> bets = new ArrayList<>();
        bets.add(bet1);
        bets.add(bet2);
        bets.add(bet3);
        bets.add(bet4);
        betRepository.setBets(marketId, bets);

        SettleMarketRequestModel request = new SettleMarketRequestModel(marketId);

        // Act
        interactor.execute(request);

        // Assert Success
        assertTrue(presenter.successCalled);
        SettleMarketResponseModel response = presenter.successModel;
        assertNotNull(response);
        assertEquals(marketId, response.getMarketId());

        // Bet 4 should be skipped, so 3 bets settled
        assertEquals(3, response.getBetsSettled());

        // Total Payout accumulates only positive profits (won=true logic).
        // Only bet1 won (60.0). bet2 and bet3 lost (-25 and -20), code does 'totalPayout += profit' only in 'if(won)' block.
        assertEquals(60.0, response.getTotalPayout(), 0.001);

        // Verify Balances
        User updatedWinner = accountRepository.findByUsername("winner");
        assertEquals(1060, updatedWinner.getBalance());

        User updatedLoser = accountRepository.findByUsername("loser");
        assertEquals(975, updatedLoser.getBalance());

        User updatedNull = accountRepository.findByUsername("nullUser");
        assertEquals(980, updatedNull.getBalance());

        // Verify Repo Saves
        // Settlement records: 3 saved (bet4 skipped)
        assertEquals(3, settlementRecordRepository.savedRecords.size());
        // Bet updates: 3 saved (bet4 skipped)
        assertEquals(3, betRepository.savedBets.size());
    }

    // =========================================================================
    // Fakes / Stubs / Spies
    // =========================================================================

    private static class FakeBetRepository implements BetRepository {
        private boolean returnNull = false;
        private List<Bet> bets = new ArrayList<>();
        private List<Bet> savedBets = new ArrayList<>();

        void setReturnNull(boolean val) { this.returnNull = val; }
        void setBets(String marketId, List<Bet> bets) { this.bets = bets; }

        @Override
        public List<Bet> findByMarketId(String marketId) {
            if (returnNull) return null;
            return bets;
        }

        @Override
        public void save(Bet bet) {
            savedBets.add(bet);
        }
    }

    private static class FakeAccountRepository implements AccountRepository {
        private List<User> users = new ArrayList<>();

        @Override
        public User findByUsername(String username) {
            return users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
        }

        @Override
        public void save(User user) {
            // Remove existing if any, then add
            users.removeIf(u -> u.getUsername().equals(user.getUsername()));
            users.add(user);
        }
    }

    private static class FakeSettlementRecordRepository implements SettlementRecordRepository {
        private List<SettlementRecord> savedRecords = new ArrayList<>();

        @Override
        public Optional<SettlementRecord> findByMarketId(String marketId) {
            return Optional.empty();
        }

        @Override
        public void save(SettlementRecord record) {
            savedRecords.add(record);
        }
    }

    private static class SpyPresenter implements SettleMarketOutputBoundary {
        boolean successCalled = false;
        boolean failureCalled = false;
        SettleMarketResponseModel successModel;
        String failureMessage;

        @Override
        public void presentSuccess(SettleMarketResponseModel responseModel) {
            successCalled = true;
            successModel = responseModel;
        }

        @Override
        public void presentFailure(String errorMessage) {
            failureCalled = true;
            failureMessage = errorMessage;
        }
    }
}
