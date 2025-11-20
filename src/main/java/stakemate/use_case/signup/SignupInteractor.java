package stakemate.use_case.signup;

import stakemate.entity.User;

public class SignupInteractor implements SignupInputBoundary {
    private final SignupUserDataAccessInterface userDataAccess;
    private final SignupOutputBoundary presenter;

    public SignupInteractor(SignupUserDataAccessInterface userDataAccess,
                            SignupOutputBoundary presenter) {
        this.userDataAccess = userDataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(SignupInputData inputData) {
        String username = inputData.getUsername();
        String password = inputData.getPassword();

        if (username == null || username.isEmpty() ||
                password == null || password.isEmpty()) {
            presenter.prepareFailView("Username and password cannot be empty.");
            return;
        }

        if (userDataAccess.existsByUsername(username)) {
            presenter.prepareFailView("Username already exists.");
            return;
        }

        // Give every user a starting bankroll, e.g., 10_000 credits
        User newUser = new User(username, password, 10_000);
        userDataAccess.save(newUser);

        SignupOutputData outputData = new SignupOutputData(username);
        presenter.prepareSuccessView(outputData);
    }
}
