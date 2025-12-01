package stakemate.use_case.signup;

import stakemate.entity.User;

/**
 * Data Access Interface for the Signup Use Case.
 */
public interface SignupUserDataAccessInterface {
    /**
     * Checks if a user exists by username.
     * 
     * @param username the username.
     * @return true if the user exists, false otherwise.
     */
    boolean existsByUsername(String username);

    /**
     * Saves a new user.
     * 
     * @param user the user to save.
     */
    void save(User user);
}
