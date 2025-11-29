package stakemate.interface_adapter.view_signup;

/**
 * Interface for the Signup View.
 */
public interface SignupView {
    /**
     * Shows an error message.
     * 
     * @param message the error message.
     */
    void showError(String message);

    /**
     * Handles successful signup.
     * 
     * @param username the username of the newly signed-up user.
     */
    void onSignupSuccess(String username);
}
