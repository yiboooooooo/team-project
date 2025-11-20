package stakemate.entity;

public class User {
    private final String username;
    private final String password;
    private int balance;

    public User(final String username, final String password, final int balance) {
        this.username = username;
        this.password = password;
        this.balance = balance;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(final int balance) {
        this.balance = balance;
    }
}
