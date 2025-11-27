package stakemate.use_case.view_profile;

import java.util.List;

import stakemate.entity.User;
import stakemate.use_case.settle_market.Bet;

public interface ViewProfileUserDataAccessInterface {
    User getByUsername(String username);

    List<Bet> getPositionsByUsername(String username);
}
