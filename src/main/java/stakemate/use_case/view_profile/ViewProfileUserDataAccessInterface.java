package stakemate.use_case.view_profile;

import java.util.List;

import stakemate.entity.User;
import stakemate.use_case.settle_market.Bet;

/**
 * Data Access Interface for the View Profile Use Case.
 */
public interface ViewProfileUserDataAccessInterface {
    /**
     * Gets a user by username.
     * 
     * @param username the username.
     * @return the user, or null if not found.
     */
    User getByUsername(String username);

    /**
     * Gets the positions for a user.
     * 
     * @param username the username.
     * @return the list of bets.
     */
    List<Bet> getPositionsByUsername(String username);
}
