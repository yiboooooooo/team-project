package stakemate.service;

import stakemate.engine.Trade;

/**
 * Simple account service interface and a demo in-memory implementation.
 */
public interface AccountService {
    boolean hasSufficientFunds(String userId, String marketId, double qty, Double price);
    void reserveForOrder(String userId, String orderId, double amount);
    void capture(Trade trade); // naive settlement for demo
}
