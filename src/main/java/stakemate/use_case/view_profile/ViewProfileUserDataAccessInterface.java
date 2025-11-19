package stakemate.use_case.view_profile;

import stakemate.entity.User;

public interface ViewProfileUserDataAccessInterface {
    User getByUsername(String username);
}
