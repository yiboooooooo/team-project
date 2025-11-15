package stakemate.interface_adapter.view_login;

public interface LoginView {
    void showError(String message);

    void onLoginSuccess(String username);
}
