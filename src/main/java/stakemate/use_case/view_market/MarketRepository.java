package stakemate.use_case.view_market;

import java.util.List;

import stakemate.entity.Market;

public interface MarketRepository {
    /**
     * Finds all markets associated with a specific match ID.
     *
     * @param matchId the ID of the match to search for.
     * @return a list of markets for the given match.
     * @throws RepositoryException if an error occurs during data retrieval.
     */
    List<Market> findByMatchId(String matchId) throws RepositoryException;
}
