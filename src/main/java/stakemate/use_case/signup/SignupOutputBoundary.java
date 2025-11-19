package stakemate.use_case.signup;

public interface SignupOutputBoundary {
    void prepareSuccessView(SignupOutputData data);

    void prepareFailView(String error);
}
