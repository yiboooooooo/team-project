package stakemate.use_case.login;

import stakemate.entity.User;

public interface LoginUserDataAccessInterface {
    User getByUsername(String username);
}
