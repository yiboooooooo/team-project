package stakemate.use_case.view_market.strategy;

import stakemate.use_case.view_market.MarketSummary;

import java.util.List;

/**
 * [Strategy Pattern]
 * Interface for sorting algorithms.
 */
public interface MarketSortStrategy {
    void sort(List<MarketSummary> markets);
}