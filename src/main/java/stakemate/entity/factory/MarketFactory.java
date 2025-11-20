package stakemate.entity.factory;

import stakemate.entity.Market;
import stakemate.entity.MarketStatus;

import java.util.UUID;

/**
 * [Factory Pattern]
 * Encapsulates the logic for creating different types of Market entities.
 */
public class MarketFactory {

    public static Market createMarket(String matchId, String name, boolean isOpen) {
        String id = UUID.randomUUID().toString();
        MarketStatus status = isOpen ? MarketStatus.OPEN : MarketStatus.CLOSED;
        return new Market(id, matchId, name, status);
    }

    // Example of creating a specific type (Moneyline)
    public static Market createMoneylineMarket(String matchId) {
        return createMarket(matchId, "Moneyline", true);
    }
}