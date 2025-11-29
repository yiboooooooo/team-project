package stakemate.use_case.signup;

/**
 * Output Data for the Signup Use Case.
 */
public class SignupOutputData {
    private final String username;

    /**
     * Constructs a SignupOutputData.
     * 
     * @param username the username.
     */
    public SignupOutputData(final String username) {
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
