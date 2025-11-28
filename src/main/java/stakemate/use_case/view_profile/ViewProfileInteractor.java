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

        final List<Bet> openBets = new ArrayList<>();
        final List<Bet> historicalBets = new ArrayList<>();

        for (final Bet bet : bets) {
            if (Boolean.TRUE.equals(bet.isSettled())) {
                historicalBets.add(bet);
            } else {
                openBets.add(bet);
            }
        }

        // Sort Open Bets
        final SortCriteria openCriteria = inputData.getOpenSortCriteria();
        if (openCriteria == SortCriteria.DATE) {
            openBets.sort((b1, b2) -> b2.getUpdatedAt().compareTo(b1.getUpdatedAt()));
        } else if (openCriteria == SortCriteria.SIZE) {
            // Open: amount * price descending
            openBets.sort((b1, b2) -> Double.compare(b2.getStake() * b2.getPrice(), b1.getStake() * b1.getPrice()));
        }

        // Sort Historical Bets
        final SortCriteria histCriteria = inputData.getHistoricalSortCriteria();
        if (histCriteria == SortCriteria.DATE) {
            historicalBets.sort((b1, b2) -> b2.getUpdatedAt().compareTo(b1.getUpdatedAt()));
        } else if (histCriteria == SortCriteria.SIZE) {
            // Historical: amount * (won ? 1 : 0) descending
            historicalBets.sort((b1, b2) -> {
                final double v1 = b1.getStake() * (Boolean.TRUE.equals(b1.isWon()) ? 1.0 : 0.0);
                final double v2 = b2.getStake() * (Boolean.TRUE.equals(b2.isWon()) ? 1.0 : 0.0);
                return Double.compare(v2, v1);
            });
        }

        final List<String[]> openPositions = new ArrayList<>();
        for (final Bet bet : openBets) {
            final String marketName = bet.getMarketId();
            final String team = bet.getTeamName();
            final String buyPrice = String.format("%.2f", bet.getPrice());
            final String size = String.format("%.0f", bet.getStake());

            final double buyAmtVal = bet.getPrice() * bet.getStake();
            final String buyAmt = String.format("%.2f", buyAmtVal);

            final double potentialProfitVal = (1.0 - bet.getPrice()) * bet.getStake();
            final String potentialProfit = String.format("%.2f", potentialProfitVal);

            openPositions.add(new String[] {
                    marketName, team, buyPrice, size, buyAmt, potentialProfit
            });
        }

        final List<String[]> historicalPositions = new ArrayList<>();
        for (final Bet bet : historicalBets) {
            final String marketName = bet.getMarketId();
            final String team = bet.getTeamName();
            final String buyPrice = String.format("%.2f", bet.getPrice());
            final String size = String.format("%.0f", bet.getStake());

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
