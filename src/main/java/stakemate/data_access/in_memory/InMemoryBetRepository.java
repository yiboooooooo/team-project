package stakemate.data_access.in_memory;

import java.util.ArrayList;
import java.util.List;

import stakemate.use_case.settle_market.Bet;
import stakemate.use_case.settle_market.BetRepository;

public class InMemoryBetRepository implements BetRepository {

    private final List<Bet> bets = new ArrayList<>();

    @Override
    public List<Bet> findByMarketId(final String marketId) {
        final List<Bet> result = new ArrayList<>();
        for (final Bet b : bets) {
            if (b.getMarketId().equals(marketId)) {
                result.add(b);
            }
        }
        return result;
    }

    @Override
    public void save(final Bet bet) {
        if (!bets.contains(bet)) {
            bets.add(bet);
        }
    }

    public void addDemoBet(final Bet bet) {
        bets.add(bet);
    }
}
