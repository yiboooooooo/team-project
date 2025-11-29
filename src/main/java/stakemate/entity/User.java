package stakemate.entity;

/**
 * Entity representing a User.
 */
public class User {
    private final String username;
    private final String password;
    private int balance;

    /**
     * Constructs a new User.
     * 
     * @param username the username.
     * @param password the password.
     * @param balance  the initial balance.
     */
    public User(final String username, final String password, final int balance) {
        this.username = username;
        this.password = password;
        this.balance = balance;
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

    /**
     * Gets the balance.
     * 
     * @return the balance.
     */
    public int getBalance() {
        return balance;
    }

    /**
     * Sets the balance.
     * 
     * @param balance the new balance.
     */
    public void setBalance(final int balance) {
        this.balance = balance;
    }
}
