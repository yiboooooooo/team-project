package stakemate.interface_adapter.view_login;

/**
 * State for the Login View.
 */
public class LoginState {
    private String username = "";
    private String error = "";

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
     * @param username the username to set.
     */
    public void setUsername(final String username) {
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
     * @param error the error message to set.
     */
    public void setError(final String error) {
        this.error = error;
    }
}
