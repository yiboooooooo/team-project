package stakemate.use_case.view_market.strategy;

import stakemate.use_case.view_market.MarketSummary;

import java.util.Comparator;
import java.util.List;

public class NameSortStrategy implements MarketSortStrategy {
    @Override
    public void sort(List<MarketSummary> markets) {
        markets.sort(Comparator.comparing(MarketSummary::getName));
    }
}