package stakemate.entity.factory;

import java.util.UUID;

import stakemate.entity.Market;
import stakemate.entity.MarketStatus;

/**
 * [Factory Pattern]
 * Encapsulates the logic for creating different types of Market entities.
 */
public class MarketFactory {

    public static Market createMarket(final String matchId, final String name, final boolean isOpen) {
        final String id = UUID.randomUUID().toString();
        final MarketStatus status = isOpen ? MarketStatus.OPEN : MarketStatus.CLOSED;
        return new Market(id, matchId, name, status);
    }

    // Example of creating a specific type (Moneyline)
    public static Market createMoneylineMarket(final String matchId) {
        return createMarket(matchId, "Moneyline", true);
    }
}
