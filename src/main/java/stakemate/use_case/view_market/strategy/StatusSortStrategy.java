package stakemate.use_case.view_market.strategy;

import java.util.Comparator;
import java.util.List;

import stakemate.use_case.view_market.MarketSummary;

public class StatusSortStrategy implements MarketSortStrategy {
    @Override
    public void sort(final List<MarketSummary> markets) {
        // Open markets first, then alphabetical
        markets.sort(Comparator.comparing(MarketSummary::getStatusLabel).reversed()
            .thenComparing(MarketSummary::getName));
    }
}
