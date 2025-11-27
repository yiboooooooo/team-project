package stakemate.use_case.view_profile;

import stakemate.entity.User;
import java.util.ArrayList;
import java.util.List;
import stakemate.use_case.settle_market.Bet;

public class ViewProfileInteractor implements ViewProfileInputBoundary {
    private final ViewProfileUserDataAccessInterface userDataAccess;
    private final ViewProfileOutputBoundary outputBoundary;

    public ViewProfileInteractor(ViewProfileUserDataAccessInterface userDataAccess,
            ViewProfileOutputBoundary outputBoundary) {
        this.userDataAccess = userDataAccess;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(ViewProfileInputData inputData) {
        String username = inputData.getUsername();
        User user = userDataAccess.getByUsername(username);

        if (user == null) {
            outputBoundary.presentError("User not found: " + username);
            return;
        }

        final java.util.List<Bet> bets = userDataAccess.getPositionsByUsername(username);

        final List<String[]> openPositions = new ArrayList<>();
        final List<String[]> historicalPositions = new ArrayList<>();

        for (final Bet bet : bets) {
            final String marketName = bet.getMarketId(); // Now contains "Team A vs Team B" or fallback
            final String team = bet.getTeamName(); // Now contains resolved team name
            final String buyPrice = String.format("%.2f", bet.getPrice());
            final String size = String.format("%.0f", bet.getStake());

            if (Boolean.TRUE.equals(bet.isSettled())) {
                // Historical
                double profitVal = 0.0;
                if (Boolean.TRUE.equals(bet.isWon())) {
                    profitVal = (1.0 - bet.getPrice()) * bet.getStake();
                } else {
                    profitVal = -1.0 * bet.getPrice() * bet.getStake();
                }
                final String profit = String.format("%.2f", profitVal);

                historicalPositions.add(new String[] {
                        marketName, team, buyPrice, size, profit
                });
            } else {
                // Open
                final double buyAmtVal = bet.getPrice() * bet.getStake();
                final String buyAmt = String.format("%.2f", buyAmtVal);

                final double potentialProfitVal = (1.0 - bet.getPrice()) * bet.getStake();
                final String potentialProfit = String.format("%.2f", potentialProfitVal);

                openPositions.add(new String[] {
                        marketName, team, buyPrice, size, buyAmt, potentialProfit
                });
            }
        }

        final ViewProfileOutputData outputData = new ViewProfileOutputData(
                user.getUsername(),
                user.getBalance(),
                user.getBalance() - 10000, // PnL = current balance - starting balance (10,000)
                openPositions,
                historicalPositions);

        outputBoundary.presentProfile(outputData);
    }
}
