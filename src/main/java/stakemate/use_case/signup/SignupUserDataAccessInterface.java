package stakemate.use_case.signup;

import stakemate.entity.User;

public interface SignupUserDataAccessInterface {
    boolean existsByUsername(String username);

    void save(User user);
}
