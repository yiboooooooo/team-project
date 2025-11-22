package stakemate.use_case.view_market.strategy;

import java.util.List;

import stakemate.use_case.view_market.MarketSummary;

/**
 * [Strategy Pattern]
 * Interface for sorting algorithms.
 */
public interface MarketSortStrategy {
    void sort(List<MarketSummary> markets);
}
