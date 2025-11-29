package stakemate.use_case.signup;

/**
 * Input Data for the Signup Use Case.
 */
public class SignupInputData {
    private final String username;
    private final String password;

    /**
     * Constructs a SignupInputData.
     * 
     * @param username the username.
     * @param password the password.
     */
    public SignupInputData(final String username, final String password) {
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
