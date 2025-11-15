package stakemate.interface_adapter.view_signup;

public interface SignupView {
    void showError(String message);

    void onSignupSuccess(String username);
}
