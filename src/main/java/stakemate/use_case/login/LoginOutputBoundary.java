package stakemate.use_case.login;

public interface LoginOutputBoundary {
    void prepareSuccessView(LoginOutputData data);
    void prepareFailView(String error);
}
