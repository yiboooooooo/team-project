package stakemate.data_access.in_memory;

import stakemate.entity.Market;
import stakemate.entity.MarketStatus;
import stakemate.use_case.view_market.MarketRepository;
import stakemate.use_case.view_market.RepositoryException;

import java.util.ArrayList;
import java.util.List;

public class InMemoryMarketRepository implements MarketRepository {

    private final List<Market> markets = new ArrayList<>();

    public InMemoryMarketRepository() {
        markets.add(new Market("M1-ML", "M1", "Moneyline", MarketStatus.OPEN));
        markets.add(new Market("M1-TOTAL", "M1", "Total Points O/U", MarketStatus.OPEN));

        markets.add(new Market("M2-ML", "M2", "Moneyline", MarketStatus.OPEN));
        markets.add(new Market("M2-SPREAD", "M2", "Point Spread", MarketStatus.OPEN));

        markets.add(new Market("M3-ML", "M3", "Moneyline", MarketStatus.CLOSED));
    }

    @Override
    public List<Market> findByMatchId(String matchId) throws RepositoryException {
        List<Market> result = new ArrayList<>();
        for (Market m : markets) {
            if (m.getMatchId().equals(matchId)) {
                result.add(m);
            }
        }
        return result;
    }
}
