package stakemate.use_case.login;

import stakemate.entity.User;

public class LoginInteractor implements LoginInputBoundary {
    private final LoginUserDataAccessInterface userDataAccess;
    private final LoginOutputBoundary presenter;

    public LoginInteractor(LoginUserDataAccessInterface userDataAccess,
                           LoginOutputBoundary presenter) {
        this.userDataAccess = userDataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(LoginInputData inputData) {
        String username = inputData.getUsername();
        String password = inputData.getPassword();

        User user = userDataAccess.getByUsername(username);
        if (user == null) {
            presenter.prepareFailView("User does not exist.");
            return;
        }

        if (!user.getPassword().equals(password)) {
            presenter.prepareFailView("Incorrect password.");
            return;
        }

        LoginOutputData outputData = new LoginOutputData(username);
        presenter.prepareSuccessView(outputData);
    }
}
