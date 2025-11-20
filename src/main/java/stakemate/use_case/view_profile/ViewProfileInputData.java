package stakemate.use_case.view_profile;

public class ViewProfileInputData {
    private final String username;

    public ViewProfileInputData(final String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
