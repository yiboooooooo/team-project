package stakemate.use_case.settle_market;

import stakemate.entity.User;

public interface AccountRepository {

    User findByUsername(String username);

    void save(User user);
}
