package stakemate.use_case.settle_market;

import java.time.LocalDateTime;
import java.util.List;

import stakemate.entity.User;

/**
 * Interactor for the Settle Market use case.
 * Implements the business logic for settling bets on a market.
 */
public class SettleMarketInteractor implements SettleMarketInputBoundary {

    private final BetRepository betRepository;
    private final AccountRepository accountRepository;
    private final SettlementRecordRepository settlementRecordRepository;
    private final SettleMarketOutputBoundary presenter;

    /**
     * Constructs a new SettleMarketInteractor.
     *
     * @param betRepository              the repository for accessing bets.
     * @param accountRepository          the repository for accessing user accounts.
     * @param settlementRecordRepository the repository for storing settlement records.
     * @param presenter                  the output boundary to present results.
     */
    public SettleMarketInteractor(final BetRepository betRepository,
                                  final AccountRepository accountRepository,
                                  final SettlementRecordRepository settlementRecordRepository,
                                  final SettleMarketOutputBoundary presenter) {

        this.betRepository = betRepository;
        this.accountRepository = accountRepository;
        this.settlementRecordRepository = settlementRecordRepository;
        this.presenter = presenter;
    }

    @Override
    public void execute(final SettleMarketRequestModel requestModel) {

        final String marketId = requestModel.getMarketId();
        System.out.println("DEBUG - Settling market: " + marketId);

        final List<Bet> bets = betRepository.findByMarketId(marketId);
        if (bets == null || bets.isEmpty()) {
            presenter.presentFailure("No bets found for market " + marketId);
        }
        else {
            settleBets(marketId, bets);
        }
    }

    private void settleBets(String marketId, List<Bet> bets) {
        int settledCount = 0;
        double totalPayout = 0.0;

        for (final Bet bet : bets) {
            final User user = accountRepository.findByUsername(bet.getUsername());
            if (user == null) {
                System.out.println("WARN - No user found for bet: " + bet.getUsername());
                continue;
            }

            final boolean won = Boolean.TRUE.equals(bet.isWon());
            final double payout = calculatePayout(bet, won);

            final int newBalance = (int) Math.round(user.getBalance() + payout);
            user.setBalance(newBalance);
            accountRepository.save(user);

            if (won) {
                totalPayout += payout;
            }

            saveSettlementRecord(marketId, bet, payout, won);
            betRepository.save(bet);

            settledCount++;
        }

        final SettleMarketResponseModel response =
            new SettleMarketResponseModel(marketId, settledCount, totalPayout);

        presenter.presentSuccess(response);
    }

    private double calculatePayout(Bet bet, boolean won) {
        final double payout;
        if (won) {
            payout = bet.getStake() * (1 - bet.getPrice());
        }
        else {
            payout = -bet.getStake() * bet.getPrice();
        }
        return payout;
    }

    private void saveSettlementRecord(String marketId, Bet bet, double payout, boolean won) {
        final SettlementRecord record = new SettlementRecord(
            marketId,
            bet.getUsername(),
            bet.getStake(),
            payout,
            won,
            LocalDateTime.now()
        );
        settlementRecordRepository.save(record);
    }
}
