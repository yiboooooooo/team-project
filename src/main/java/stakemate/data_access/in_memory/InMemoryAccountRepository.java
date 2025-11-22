package stakemate.data_access.in_memory;

import java.util.HashMap;
import java.util.Map;

import stakemate.entity.User;
import stakemate.use_case.settle_market.AccountRepository;

public class InMemoryAccountRepository implements AccountRepository {

    private final Map<String, User> users = new HashMap<>();

    @Override
    public User findByUsername(final String username) {
        return users.get(username);
    }

    @Override
    public void save(final User user) {
        users.put(user.getUsername(), user);
    }

    // Helper for your demo to pre-load users with balances
    public void addDemoUser(final User user) {
        users.put(user.getUsername(), user);
    }
}
