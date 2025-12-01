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
        final boolean homeWon = requestModel.isHomeTeamWon(); // Get the outcome

        final List<Bet> bets = betRepository.findByMarketId(marketId);
        if (bets == null || bets.isEmpty()) {
            presenter.presentFailure("No bets found for this market ");
        } else {
            settleBets(marketId, bets, homeWon);
        }
    }

    private void settleBets(String marketId, List<Bet> bets, boolean homeWon) {
        int settledCount = 0;
        double totalPayout = 0.0;
        StringBuilder summary = new StringBuilder("Settlement Results:\n");

        for (final Bet bet : bets) {
            final User user = accountRepository.findByUsername(bet.getUsername());
            if (user == null) continue;

            boolean isWinner = false;
            if (bet.getSide() == stakemate.entity.Side.BUY && homeWon) {
                isWinner = true;
            } else if (bet.getSide() == stakemate.entity.Side.SELL && !homeWon) {
                isWinner = true;
            }

            final double payout = calculatePayout(bet, isWinner);

            if (payout > 0) {
                final int newBalance = (int) Math.round(user.getBalance() + payout);
                user.setBalance(newBalance);
                accountRepository.save(user);
                totalPayout += payout;
            }

            Bet settledBet = new Bet(
                bet.getUsername(),
                bet.getMarketId(),
                bet.getSide(),
                bet.getStake(),
                bet.getPrice(),
                isWinner,
                true,
                bet.getTeamName(),
                java.time.Instant.now()
            );

            saveSettlementRecord(marketId, settledBet, payout, isWinner);
            betRepository.save(settledBet);


            double cost = bet.getStake() * bet.getPrice();
            double netResult = isWinner ? (payout - cost) : -cost;

            summary.append(String.format("- %s: %s ($%.2f)\n",
                bet.getUsername(),
                isWinner ? "WON" : "LOST",
                netResult));

            settledCount++;
        }


        final SettleMarketResponseModel response =
            new SettleMarketResponseModel(marketId, settledCount, totalPayout, summary.toString());

        presenter.presentSuccess(response);
    }

    private double calculatePayout(Bet bet, boolean won) {
        final double payout;
        if (won) {
            payout = bet.getStake() * (1 - bet.getPrice()) + bet.getPrice()*bet.getStake();
        }
        else {
            payout = 0;
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
