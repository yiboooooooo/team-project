package stakemate.use_case.signup;

import stakemate.entity.User;

/**
 * Interactor for the Signup Use Case.
 */
public class SignupInteractor implements SignupInputBoundary {
    private final SignupUserDataAccessInterface userDataAccess;
    private final SignupOutputBoundary presenter;

    /**
     * Constructs a SignupInteractor.
     * 
     * @param userDataAccess the data access interface.
     * @param presenter      the output boundary.
     */
    public SignupInteractor(final SignupUserDataAccessInterface userDataAccess,
            final SignupOutputBoundary presenter) {
        this.userDataAccess = userDataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(final SignupInputData inputData) {
        final String username = inputData.getUsername();
        final String password = inputData.getPassword();

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
        final User newUser = new User(username, password, 10_000);
        userDataAccess.save(newUser);

        final SignupOutputData outputData = new SignupOutputData(username);
        presenter.prepareSuccessView(outputData);
    }
}
