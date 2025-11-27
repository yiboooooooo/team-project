package stakemate.use_case.view_market.strategy;

import java.util.List;

import stakemate.use_case.view_market.MarketSummary;

/**
 * [Strategy Pattern]
 * Interface for sorting algorithms.
 */
public interface MarketSortStrategy {
    /**
     * Sorts a list of market summaries according to the strategy implementation.
     *
     * @param markets the list of markets to sort.
     */
    void sort(List<MarketSummary> markets);
}
