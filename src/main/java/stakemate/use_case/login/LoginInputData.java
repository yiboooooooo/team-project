package stakemate.use_case.login;

public class LoginInputData {
    private final String username;
    private final String password;

    public LoginInputData(final String username, final String password) {
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
