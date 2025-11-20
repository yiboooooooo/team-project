package stakemate.use_case.settle_market;

import stakemate.entity.Side;
import stakemate.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public class SettleMarketInteractor implements SettleMarketInputBoundary {

    private final BetRepository betRepository;
    private final AccountRepository accountRepository;
    private final SettlementRecordRepository settlementRecordRepository;
    private final SettleMarketOutputBoundary presenter;

    public SettleMarketInteractor(BetRepository betRepository,
                                  AccountRepository accountRepository,
                                  SettlementRecordRepository settlementRecordRepository,
                                  SettleMarketOutputBoundary presenter) {

        this.betRepository = betRepository;
        this.accountRepository = accountRepository;
        this.settlementRecordRepository = settlementRecordRepository;
        this.presenter = presenter;
    }

    @Override
    public void execute(SettleMarketRequestModel requestModel) {

        String marketId = requestModel.getMarketId();
        boolean homeTeamWon = requestModel.isHomeTeamWon();

        // 1) Load all bets for this market
        List<Bet> bets = betRepository.findByMarketId(marketId);

        if (bets == null || bets.isEmpty()) {
            presenter.presentFailure("No bets found for market " + marketId);
            return;
        }

        // 2) Decide which Side is the winner for this demo
        Side winningSide = homeTeamWon ? Side.BUY : Side.SELL;

        int settledCount = 0;
        double totalPayout = 0.0; // total money paid out to winners (sum of their profit)

        // 3) Loop over all bets and update balances
        for (Bet bet : bets) {

            boolean won = (bet.getSide() == winningSide);

            // net change to this user's balance (positive = gain, negative = loss)
            double payout = 0.0;

            // Find the user once here
            User user = accountRepository.findByUsername(bet.getUsername());
            if (user == null) {
                // Skip this bet if somehow the user doesn't exist
                continue;
            }

            if (won) {
                // Winner: profit = stake * (1 - price)
                double profit = bet.getStake() * (1 - bet.getPrice());
                payout = profit;  // net change to balance (positive)

                int newBalance = (int) Math.round(user.getBalance() + payout);
                user.setBalance(newBalance);
                accountRepository.save(user);

                // Only winners contribute to totalPayout
                totalPayout += profit;

            } else {
                // Loser: lose stake * price (negative payout)
                payout = -bet.getStake() * bet.getPrice();

                int newBalance = (int) Math.round(user.getBalance() + payout);
                user.setBalance(newBalance);
                accountRepository.save(user);
            }

            // Record this individual settlement (payout is net change, can be + or -)
            SettlementRecord record = new SettlementRecord(
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
        SettleMarketResponseModel response =
            new SettleMarketResponseModel(marketId, settledCount, totalPayout);

        presenter.presentSuccess(response);
    }


}
