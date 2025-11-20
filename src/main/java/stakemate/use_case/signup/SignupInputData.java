package stakemate.use_case.signup;

public class SignupInputData {
    private final String username;
    private final String password;

    public SignupInputData(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
