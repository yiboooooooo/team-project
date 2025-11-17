package stakemate.service;

import stakemate.engine.Trade;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Very small demo account service. Balances tracked per user; reservations tracked per orderId.
 */
public class InMemoryAccountService implements AccountService {

    private final Map<String, Double> balances = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Double>> reservations = new ConcurrentHashMap<>();

    public InMemoryAccountService() {
        // seed demo users (you can top up in the UI)
        balances.put("alice", 1000.0);
        balances.put("bob", 1000.0);
    }

    @Override
    public boolean hasSufficientFunds(String userId, String marketId, double qty, Double price) {
        double p = (price == null) ? 1.0 : price;
        double need = p * qty;
        double reserved = reservations.getOrDefault(userId, Map.of()).values().stream().mapToDouble(Double::doubleValue).sum();
        double available = balances.getOrDefault(userId, 0.0) - reserved;
        return available + 1e-9 >= need;
    }

    @Override
    public void reserveForOrder(String userId, String orderId, double amount) {
        reservations.computeIfAbsent(userId, k -> new ConcurrentHashMap<>()).put(orderId, amount);
    }

    @Override
    public void capture(Trade trade) {
        // naive: buyer pays total to seller
        // find buyer/seller IDs from trade object (we saved IDs only) -> in demo we don't have access to userIDs here.
        // For demo clarity, skip complex settlement. (You may extend this to map orderId->userId to settle precisely.)
    }

    // demo helper methods
    public void deposit(String user, double amount) { balances.put(user, balances.getOrDefault(user,0.0) + amount); }
    public double getBalance(String user) { return balances.getOrDefault(user, 0.0); }
}
