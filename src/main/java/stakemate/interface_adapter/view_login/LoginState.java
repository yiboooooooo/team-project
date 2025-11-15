package stakemate.interface_adapter.view_login;

public class LoginState {
    private String username = "";
    private String error = "";

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
