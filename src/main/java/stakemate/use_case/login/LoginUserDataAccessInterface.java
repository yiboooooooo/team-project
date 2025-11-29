package stakemate.use_case.login;

import stakemate.entity.User;

/**
 * Data Access Interface for the Login Use Case.
 */
public interface LoginUserDataAccessInterface {
    /**
     * Gets a user by username.
     * 
     * @param username the username.
     * @return the user, or null if not found.
     */
    User getByUsername(String username);
}
