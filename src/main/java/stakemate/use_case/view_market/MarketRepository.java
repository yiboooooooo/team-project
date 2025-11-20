package stakemate.use_case.view_market;

import stakemate.entity.Market;

import java.util.List;

public interface MarketRepository {
    List<Market> findByMatchId(String matchId) throws RepositoryException;
}
