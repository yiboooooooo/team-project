package stakemate.use_case.settle_market;

import java.time.LocalDateTime;
import java.util.List;

import stakemate.entity.User;

public class SettleMarketInteractor implements SettleMarketInputBoundary {

    private final BetRepository betRepository;
    private final AccountRepository accountRepository;
    private final SettlementRecordRepository settlementRecordRepository;
    private final SettleMarketOutputBoundary presenter;

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
        System.out.println("DEBUG — Settling market: " + marketId);

        final List<Bet> bets = betRepository.findByMarketId(marketId);
        if (bets == null || bets.isEmpty()) {
            presenter.presentFailure("No bets found for market " + marketId);
            return;
        }

        int settledCount = 0;
        double totalPayout = 0.0;

        for (final Bet bet : bets) {

            final Boolean wonFlag = bet.isWon();
            final boolean won = Boolean.TRUE.equals(wonFlag);

            double payout = 0.0;

            final User user = accountRepository.findByUsername(bet.getUsername());
            if (user == null) {
                System.out.println("WARN — No user found for bet: " + bet.getUsername());
                continue;
            }

            if (won) {
                final double profit = bet.getStake() * (1 - bet.getPrice());
                payout = profit;

                final int newBalance = (int) Math.round(user.getBalance() + payout);
                user.setBalance(newBalance);
                accountRepository.save(user);

                totalPayout += profit;
            } else {
                payout = -bet.getStake() * bet.getPrice();

                final int newBalance = (int) Math.round(user.getBalance() + payout);
                user.setBalance(newBalance);
                accountRepository.save(user);
            }

            final SettlementRecord record = new SettlementRecord(
                marketId,
                bet.getUsername(),
                bet.getStake(),
                payout,
                won,
                LocalDateTime.now()
            );

            settlementRecordRepository.save(record);

            betRepository.save(bet);

            settledCount++;
        }

        final SettleMarketResponseModel response =
            new SettleMarketResponseModel(marketId, settledCount, totalPayout);

        presenter.presentSuccess(response);
    }
}
