package stakemate.use_case.login;

/**
 * Output Data for the Login Use Case.
 */
public class LoginOutputData {
    private final String username;

    /**
     * Constructs a LoginOutputData.
     * 
     * @param username the username.
     */
    public LoginOutputData(final String username) {
        this.username = username;
    }

    /**
     * Gets the username.
     * 
     * @return the username.
     */
    public String getUsername() {
        return username;
    }
}
