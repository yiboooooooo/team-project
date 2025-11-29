package stakemate.interface_adapter.view_login;

/**
 * Interface for the Login View.
 */
public interface LoginView {
    /**
     * Shows an error message.
     * 
     * @param message the error message.
     */
    void showError(String message);

    /**
     * Handles successful login.
     * 
     * @param username the username of the logged-in user.
     */
    void onLoginSuccess(String username);
}
