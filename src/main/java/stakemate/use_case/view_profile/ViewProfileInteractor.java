package stakemate.use_case.view_profile;

import java.util.ArrayList;
import java.util.List;

import stakemate.entity.User;

public class ViewProfileInteractor implements ViewProfileInputBoundary {
    private final ViewProfileUserDataAccessInterface userDataAccess;
    private final ViewProfileOutputBoundary outputBoundary;

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

        // Hardcoded exemplar data as requested
        final List<String[]> openPositions = new ArrayList<>();
        // Won}
        openPositions.add(new String[]{
            "Lakers vs Warriors", "Lakers", "0.60", "100", "60.00", "40.00"
        });

        final List<String[]> historicalPositions = new ArrayList<>();
        historicalPositions.add(new String[]{
            "Knicks vs Celtics", "Celtics", "0.40", "50", "30.00"
        });

        final ViewProfileOutputData outputData = new ViewProfileOutputData(
            user.getUsername(),
            user.getBalance(),
            0, // PnL set to 0 for now
            openPositions,
            historicalPositions);

        outputBoundary.presentProfile(outputData);
    }
}
