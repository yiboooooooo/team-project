package stakemate.use_case.settle_market;

import stakemate.entity.User;

/**
 * Repository interface for accessing and persisting user account data.
 */
public interface AccountRepository {

    /**
     * Finds a user by their username.
     *
     * @param username the username to search for.
     * @return the User entity if found.
     */
    User findByUsername(String username);

    /**
     * Saves the current state of a user to the repository.
     *
     * @param user the User entity to save.
     */
    void save(User user);
}
