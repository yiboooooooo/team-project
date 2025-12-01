package stakemate.interface_adapter.view_signup;

/**
 * State for the Signup View.
 */
public class SignupState {
    private String username = "";
    private String error;

    /**
     * Gets the username.
     * 
     * @return the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     * 
     * @param username the username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the error message.
     * 
     * @return the error message.
     */
    public String getError() {
        return error;
    }

    /**
     * Sets the error message.
     * 
     * @param error the error message.
     */
    public void setError(String error) {
        this.error = error;
    }
}
