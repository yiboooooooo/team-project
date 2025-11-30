package stakemate.use_case.settle_market;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stakemate.entity.Side;
import stakemate.entity.User;

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
        // Covers branch: if (bets == null)
        betRepository.setReturnNull(true);
        SettleMarketRequestModel request = new SettleMarketRequestModel("m1", true);

        interactor.execute(request);

        assertTrue(presenter.failureCalled);
        assertEquals("No bets found for market m1", presenter.failureMessage);
    }

    @Test
    void testExecute_EmptyBets_ReturnsFailure() {
        SettleMarketRequestModel request = new SettleMarketRequestModel("m1", true);

        interactor.execute(request);

        assertTrue(presenter.failureCalled);
        assertEquals("No bets found for market m1", presenter.failureMessage);
    }

    @Test
    void testExecute_UserNotFound_SkipsBet() {
        String marketId = "m_ghost";

        Bet ghostBet = new Bet("ghost", marketId, Side.BUY, 100.0, 0.5, null, false);

        List<Bet> bets = new ArrayList<>();
        bets.add(ghostBet);
        betRepository.setBets(marketId, bets);

        SettleMarketRequestModel request = new SettleMarketRequestModel(marketId, true);
        interactor.execute(request);

        assertTrue(presenter.successCalled);
        assertEquals(0, presenter.successModel.getBetsSettled());
        assertEquals(0, betRepository.savedBets.size());
    }

    @Test
    void testExecute_HomeTeamWins() {
        String marketId = "m_home";
        User alice = new User("alice", "pass", 1000);
        User bob = new User("bob", "pass", 1000);
        accountRepository.save(alice);
        accountRepository.save(bob);

        Bet betAlice = new Bet("alice", marketId, Side.BUY, 100.0, 0.4, null, false);
        Bet betBob = new Bet("bob", marketId, Side.SELL, 100.0, 0.6, null, false);

        List<Bet> bets = new ArrayList<>();
        bets.add(betAlice);
        bets.add(betBob);
        betRepository.setBets(marketId, bets);

        interactor.execute(new SettleMarketRequestModel(marketId, true));

        assertTrue(presenter.successCalled);
        SettleMarketResponseModel response = presenter.successModel;

        assertEquals(60.0, response.getTotalPayout(), 0.001);

        assertTrue(betRepository.savedBets.get(0).isWon());
        assertFalse(betRepository.savedBets.get(1).isWon());
    }

    @Test
    void testExecute_AwayTeamWins() {
        String marketId = "m_away";
        User alice = new User("alice", "pass", 1000);
        User bob = new User("bob", "pass", 1000);
        accountRepository.save(alice);
        accountRepository.save(bob);

        Bet betAlice = new Bet("alice", marketId, Side.BUY, 100.0, 0.4, null, false);
        // Bob SELL (Wins because Away won)
        Bet betBob = new Bet("bob", marketId, Side.SELL, 100.0, 0.6, null, false);

        List<Bet> bets = new ArrayList<>();
        bets.add(betAlice);
        bets.add(betBob);
        betRepository.setBets(marketId, bets);

        interactor.execute(new SettleMarketRequestModel(marketId, false));

        assertTrue(presenter.successCalled);

        assertFalse(betRepository.savedBets.get(0).isWon());
        assertTrue(betRepository.savedBets.get(1).isWon());
    }


    private static class FakeBetRepository implements BetRepository {
        private boolean returnNull = false;
        private List<Bet> bets = new ArrayList<>();
        public List<Bet> savedBets = new ArrayList<>();

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
            users.removeIf(u -> u.getUsername().equals(user.getUsername()));
            users.add(user);
        }
    }

    private static class FakeSettlementRecordRepository implements SettlementRecordRepository {
        public List<SettlementRecord> savedRecords = new ArrayList<>();

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
