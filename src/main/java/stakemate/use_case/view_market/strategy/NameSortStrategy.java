package stakemate.use_case.view_market.strategy;

import java.util.Comparator;
import java.util.List;

import stakemate.use_case.view_market.MarketSummary;

public class NameSortStrategy implements MarketSortStrategy {
    @Override
    public void sort(final List<MarketSummary> markets) {
        markets.sort(Comparator.comparing(MarketSummary::getName));
    }
}
