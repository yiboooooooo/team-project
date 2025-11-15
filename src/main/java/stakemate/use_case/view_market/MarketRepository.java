package stakemate.use_case.view_market;

import java.util.List;

import stakemate.entity.Market;

public interface MarketRepository {
    List<Market> findByMatchId(String matchId) throws RepositoryException;
}
