package stakemate.use_case.login;

public class LoginOutputData {
    private final String username;

    public LoginOutputData(final String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
