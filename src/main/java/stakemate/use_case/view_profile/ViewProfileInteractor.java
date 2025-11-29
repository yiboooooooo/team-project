package stakemate.use_case.view_profile;

import java.util.ArrayList;
import java.util.List;

import stakemate.entity.User;
import stakemate.use_case.settle_market.Bet;
import stakemate.use_case.view_profile.strategy.BetComparator;

/**
 * Interactor for the View Profile Use Case.
 */
public class ViewProfileInteractor implements ViewProfileInputBoundary {
    private final ViewProfileUserDataAccessInterface userDataAccess;
    private final ViewProfileOutputBoundary outputBoundary;

    /**
     * Constructs a ViewProfileInteractor.
     * 
     * @param userDataAccess the data access interface.
     * @param outputBoundary the output boundary.
     */
    public ViewProfileInteractor(final ViewProfileUserDataAccessInterface userDataAccess,
            final ViewProfileOutputBoundary outputBoundary) {
        this.userDataAccess = userDataAccess;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(final ViewProfileInputData inputData) {
        final String username = inputData.getUsername();
        final User user = userDataAccess.getByUsername(username);

        if (user == null) {
            outputBoundary.presentError("User not found: " + username);
            return;
        }

        final List<Bet> bets = userDataAccess.getPositionsByUsername(username);

        final List<Bet> openBets = new ArrayList<>();
        final List<Bet> historicalBets = new ArrayList<>();

        for (final Bet bet : bets) {
            if (Boolean.TRUE.equals(bet.isSettled())) {
                historicalBets.add(bet);
            } else {
                openBets.add(bet);
            }
        }

        // Sort Open Bets using Strategy
        final BetComparator openStrategy = inputData.getOpenSortStrategy();
        if (openStrategy != null) {
            openBets.sort(openStrategy);
        }

        // Sort Historical Bets using Strategy
        final BetComparator histStrategy = inputData.getHistoricalSortStrategy();
        if (histStrategy != null) {
            historicalBets.sort(histStrategy);
        }

        final ViewProfileOutputData outputData = new ViewProfileOutputData(
                user.getUsername(),
                user.getBalance(),
                user.getBalance() - 10000, // PnL = current balance - starting balance (10,000)
                openBets,
                historicalBets);

        outputBoundary.presentProfile(outputData);
    }
}
