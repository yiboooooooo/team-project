package stakemate.use_case.view_market.strategy;

import stakemate.use_case.view_market.MarketSummary;

import java.util.Comparator;
import java.util.List;

public class StatusSortStrategy implements MarketSortStrategy {
    @Override
    public void sort(List<MarketSummary> markets) {
        // Open markets first, then alphabetical
        markets.sort(Comparator.comparing(MarketSummary::getStatusLabel).reversed()
            .thenComparing(MarketSummary::getName));
    }
}
