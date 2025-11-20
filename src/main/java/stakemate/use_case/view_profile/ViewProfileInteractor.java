package stakemate.use_case.view_profile;

import stakemate.entity.User;

import java.util.ArrayList;
import java.util.List;

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

        // Hardcoded exemplar data as requested
        List<String[]> openPositions = new ArrayList<>();
        // {Market Name, Team, Buy in Price, Position Size, Buy in Dollar Amt, Profit if
        // Won}
        openPositions.add(new String[]{
                "Lakers vs Warriors", "Lakers", "0.60", "100", "60.00", "40.00"
        });

        List<String[]> historicalPositions = new ArrayList<>();
        // {Market Name, Team, Buy in Price, Position Size, Profit}
        historicalPositions.add(new String[]{
                "Knicks vs Celtics", "Celtics", "0.40", "50", "30.00"
        });

        ViewProfileOutputData outputData = new ViewProfileOutputData(
                user.getUsername(),
                user.getBalance(),
                0, // PnL set to 0 for now
                openPositions,
                historicalPositions);

        outputBoundary.presentProfile(outputData);
    }
}
