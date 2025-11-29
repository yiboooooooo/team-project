package stakemate.interface_adapter.view_profile;

import java.util.ArrayList;
import java.util.List;

import stakemate.use_case.settle_market.Bet;
import stakemate.use_case.view_profile.ViewProfileOutputBoundary;
import stakemate.use_case.view_profile.ViewProfileOutputData;

/**
 * Presenter for the View Profile Use Case.
 */
public class ViewProfilePresenter implements ViewProfileOutputBoundary {
    private final ProfileViewModel viewModel;

    /**
     * Constructs a ViewProfilePresenter.
     * 
     * @param viewModel the view model.
     */
    public ViewProfilePresenter(final ProfileViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentProfile(final ViewProfileOutputData outputData) {
        final ProfileState state = viewModel.getState();
        state.setUsername(outputData.getUsername());
        state.setBalance(outputData.getBalance());
        state.setPnl(outputData.getPnl());

        // Format Open Positions
        final List<String[]> openPositions = new ArrayList<>();
        for (final Bet bet : outputData.getOpenPositions()) {
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
        state.setOpenPositions(openPositions);

        // Format Historical Positions
        final List<String[]> historicalPositions = new ArrayList<>();
        for (final Bet bet : outputData.getHistoricalPositions()) {
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
        state.setHistoricalPositions(historicalPositions);

        state.setError(null);

        viewModel.setState(state);
        viewModel.firePropertyChanged();
    }

    @Override
    public void presentError(final String error) {
        final ProfileState state = viewModel.getState();
        state.setError(error);
        viewModel.setState(state);
        viewModel.firePropertyChanged();
    }
}
