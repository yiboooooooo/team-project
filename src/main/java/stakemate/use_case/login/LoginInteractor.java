package stakemate.use_case.login;

import stakemate.entity.User;

public class LoginInteractor implements LoginInputBoundary {
    private final LoginUserDataAccessInterface userDataAccess;
    private final LoginOutputBoundary presenter;

    public LoginInteractor(final LoginUserDataAccessInterface userDataAccess,
                           final LoginOutputBoundary presenter) {
        this.userDataAccess = userDataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(final LoginInputData inputData) {
        final String username = inputData.getUsername();
        final String password = inputData.getPassword();

        final User user = userDataAccess.getByUsername(username);
        if (user == null) {
            presenter.prepareFailView("User does not exist.");
            return;
        }

        if (!user.getPassword().equals(password)) {
            presenter.prepareFailView("Incorrect password.");
            return;
        }

        final LoginOutputData outputData = new LoginOutputData(username);
        presenter.prepareSuccessView(outputData);
    }
}
