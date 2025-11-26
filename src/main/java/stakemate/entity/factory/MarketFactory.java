package stakemate.entity.factory;

import java.util.UUID;

import stakemate.entity.Market;
import stakemate.entity.MarketStatus;

/**
 * [Factory Pattern]
 * Encapsulates the logic for creating different types of Market entities.
 */
public final class MarketFactory {

    private MarketFactory() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates a generic market for a match.
     *
     * @param matchId The ID of the match this market belongs to.
     * @param name    The display name of the market.
     * @param isOpen  True if the market is open for betting, false otherwise.
     * @return A new Market instance with a generated UUID.
     */
    public static Market createMarket(final String matchId, final String name, final boolean isOpen) {
        final String id = UUID.randomUUID().toString();
        final MarketStatus status;
        if (isOpen) {
            status = MarketStatus.OPEN;
        }
        else {
            status = MarketStatus.CLOSED;
        }
        return new Market(id, matchId, name, status);
    }

    /**
     * Creates a specific "Moneyline" market for a match.
     *
     * @param matchId The ID of the match.
     * @return A new Moneyline Market instance (Open by default).
     */
    public static Market createMoneylineMarket(final String matchId) {
        return createMarket(matchId, "Moneyline", true);
    }
}
