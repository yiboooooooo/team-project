package stakemate.use_case.login;

/**
 * Input Data for the Login Use Case.
 */
public class LoginInputData {
    private final String username;
    private final String password;

    /**
     * Constructs a LoginInputData.
     * 
     * @param username the username.
     * @param password the password.
     */
    public LoginInputData(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Gets the username.
     * 
     * @return the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the password.
     * 
     * @return the password.
     */
    public String getPassword() {
        return password;
    }
}
