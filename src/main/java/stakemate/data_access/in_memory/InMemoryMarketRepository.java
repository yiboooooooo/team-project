package stakemate.data_access.in_memory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import stakemate.entity.Market;
import stakemate.entity.MarketStatus;
import stakemate.use_case.view_market.MarketRepository;
import stakemate.use_case.view_market.RepositoryException;

public class InMemoryMarketRepository implements MarketRepository {

    private final List<Market> markets = new ArrayList<>();

    public InMemoryMarketRepository() {
        // Default hardcoded markets for testing with M1/M2
        markets.add(new Market("M1-ML", "M1", "Moneyline", MarketStatus.OPEN));
        markets.add(new Market("M1-TOTAL", "M1", "Total Points O/U", MarketStatus.OPEN));

        markets.add(new Market("M2-ML", "M2", "Moneyline", MarketStatus.OPEN));
        markets.add(new Market("M2-SPREAD", "M2", "Point Spread", MarketStatus.OPEN));

        markets.add(new Market("M3-ML", "M3", "Moneyline", MarketStatus.CLOSED));
    }

    @Override
    public List<Market> findByMatchId(final String matchId) throws RepositoryException {
        final List<Market> result = new ArrayList<>();

        // Check if we already have markets for this match
        for (final Market m : markets) {
            if (m.getMatchId().equals(matchId)) {
                result.add(m);
            }
        }

        // If no markets found (e.g. it's a fresh game from the API), generate defaults
        if (result.isEmpty()) {
            // Generate deterministic UUIDs based on the matchId so they stay consistent
            // We use UUIDs to be compatible if you switch to a real DB later
            final String mlId = UUID.nameUUIDFromBytes((matchId + "_Moneyline").getBytes()).toString();
            final String spreadId = UUID.nameUUIDFromBytes((matchId + "_Spread").getBytes()).toString();
            final String totalId = UUID.nameUUIDFromBytes((matchId + "_Total").getBytes()).toString();

            final Market moneyline = new Market(mlId, matchId, "Moneyline", MarketStatus.OPEN);
            final Market spread = new Market(spreadId, matchId, "Spread", MarketStatus.OPEN);
            final Market total = new Market(totalId, matchId, "Total", MarketStatus.OPEN);

            // Add to our in-memory storage
            markets.add(moneyline);
            markets.add(spread);
            markets.add(total);

            // Add to result
            result.add(moneyline);
            result.add(spread);
            result.add(total);
        }

        return result;
    }
}
