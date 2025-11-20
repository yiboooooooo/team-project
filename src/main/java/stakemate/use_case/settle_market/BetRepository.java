package stakemate.use_case.settle_market;

import java.util.List;

public interface BetRepository {
    List<Bet> findByMarketId(String marketId);

    void save(Bet bet);
}