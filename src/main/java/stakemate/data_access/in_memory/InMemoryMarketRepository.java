package stakemate.data_access.in_memory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import stakemate.entity.Market;
import stakemate.entity.MarketStatus;
import stakemate.use_case.view_market.MarketRepository;
import stakemate.use_case.view_market.RepositoryException;

public class InMemoryMarketRepository implements MarketRepository {

    private static final String MONEYLINE = "Moneyline";
    private static final String SPREAD = "Spread";
    private static final String TOTAL = "Total";
    private static final String UNDERSCORE = "_";

    private final stakemate.use_case.fetch_games.GameRepository gameRepository;

    public InMemoryMarketRepository() {
        this(null);
    }

    public InMemoryMarketRepository(final stakemate.use_case.fetch_games.GameRepository gameRepository) {
        this.gameRepository = gameRepository;

        // Default hardcoded markets for testing with M1/M2
        markets.add(new Market("M1-ML", "M1", MONEYLINE, MarketStatus.OPEN));
        markets.add(new Market("M1-TOTAL", "M1", "Total Points O/U", MarketStatus.OPEN));

        markets.add(new Market("M2-ML", "M2", MONEYLINE, MarketStatus.OPEN));
        markets.add(new Market("M2-SPREAD", "M2", "Point Spread", MarketStatus.OPEN));

        markets.add(new Market("M3-ML", "M3", MONEYLINE, MarketStatus.CLOSED));
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
            String mlId = null;

            // Try to find the real market ID from the Game entity
            if (gameRepository != null) {
                try {
                    // matchId is often the externalId or UUID string
                    java.util.Optional<stakemate.entity.Game> gameOpt = gameRepository.findByExternalId(matchId);
                    if (gameOpt.isPresent() && gameOpt.get().getMarketId() != null) {
                        mlId = gameOpt.get().getMarketId().toString();
                    }
                } catch (Exception e) {
                    // Ignore and fall back to generated ID
                    System.err.println("Error fetching game for market ID: " + e.getMessage());
                }
            }

            // Fallback if not found
            if (mlId == null) {
                mlId = UUID.nameUUIDFromBytes(
                        (matchId + UNDERSCORE + MONEYLINE).getBytes()).toString();
            }

            final String spreadId = UUID.nameUUIDFromBytes(
                    (matchId + UNDERSCORE + SPREAD).getBytes()).toString();
            final String totalId = UUID.nameUUIDFromBytes(
                    (matchId + UNDERSCORE + TOTAL).getBytes()).toString();

            final Market moneyline = new Market(mlId, matchId, MONEYLINE, MarketStatus.OPEN);
            final Market spread = new Market(spreadId, matchId, SPREAD, MarketStatus.OPEN);
            final Market total = new Market(totalId, matchId, TOTAL, MarketStatus.OPEN);

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
