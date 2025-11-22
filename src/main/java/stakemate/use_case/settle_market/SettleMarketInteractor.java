package stakemate.use_case.settle_market;

import java.time.LocalDateTime;
import java.util.List;

import stakemate.entity.Side;
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
        final boolean homeTeamWon = requestModel.isHomeTeamWon();
        final List<Bet> bets = betRepository.findByMarketId(marketId);

        if (bets == null || bets.isEmpty()) {
            presenter.presentFailure("No bets found for market " + marketId);
            return;
        }

        // 2) Decide which Side is the winner for this demo
        final Side winningSide = homeTeamWon ? Side.BUY : Side.SELL;

        int settledCount = 0;
        double totalPayout = 0.0;

        // 3) Loop over all bets and update balances
        for (final Bet bet : bets) {

            final boolean won = (bet.getSide() == winningSide);
            double payout = 0.0;
            final User user = accountRepository.findByUsername(bet.getUsername());
            if (user == null) {
                // Skip this bet if somehow the user doesn't exist
                continue;
            }

            if (won) {
                // Winner: profit = stake * (1 - price)
                final double profit = bet.getStake() * (1 - bet.getPrice());
                payout = profit;

                final int newBalance = (int) Math.round(user.getBalance() + payout);
                user.setBalance(newBalance);
                accountRepository.save(user);
                totalPayout += profit;

            }
            else {
                // Loser: lose stake * price (negative payout)
                payout = -bet.getStake() * bet.getPrice();

                final int newBalance = (int) Math.round(user.getBalance() + payout);
                user.setBalance(newBalance);
                accountRepository.save(user);
            }

            // Record this individual settlement (payout is net change, can be + or -)
            final SettlementRecord record = new SettlementRecord(
                marketId,
                bet.getUsername(),
                bet.getStake(),
                payout,
                won,
                LocalDateTime.now()
            );
            settlementRecordRepository.save(record);

            settledCount++;
        }

        // 5) Send summary back to presenter
        final SettleMarketResponseModel response =
            new SettleMarketResponseModel(marketId, settledCount, totalPayout);

        presenter.presentSuccess(response);
    }


}
