package stakemate.use_case.settle_market;

import java.util.List;

/**
 * Repository interface for accessing and persisting Bet data.
 */
public interface BetRepository {

    /**
     * Finds all bets associated with a specific market ID.
     *
     * @param marketId the ID of the market to search for.
     * @return a list of Bets placed on the given market.
     */
    List<Bet> findByMarketId(String marketId);

    /**
     * Saves a bet to the repository.
     *
     * @param bet the Bet entity to save.
     */
    void save(Bet bet);
}
