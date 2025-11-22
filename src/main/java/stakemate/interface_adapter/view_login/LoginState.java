package stakemate.interface_adapter.view_login;

public class LoginState {
    private String username = "";
    private String error = "";

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getError() {
        return error;
    }

    public void setError(final String error) {
        this.error = error;
    }
}
