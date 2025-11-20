package stakemate.use_case.signup;

public class SignupOutputData {
    private final String username;

    public SignupOutputData(final String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
