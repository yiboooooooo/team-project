package stakemate.data_access.in_memory;

import stakemate.entity.User;
import stakemate.use_case.settle_market.AccountRepository;

import java.util.HashMap;
import java.util.Map;

public class InMemoryAccountRepository implements AccountRepository {

    private final Map<String, User> users = new HashMap<>();

    @Override
    public User findByUsername(String username) {
        return users.get(username);
    }

    @Override
    public void save(User user) {
        users.put(user.getUsername(), user);
    }

    // Helper for your demo to pre-load users with balances
    public void addDemoUser(User user) {
        users.put(user.getUsername(), user);
    }
}
