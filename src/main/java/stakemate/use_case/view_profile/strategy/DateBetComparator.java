package stakemate.use_case.view_profile.strategy;

import stakemate.use_case.settle_market.Bet;

/**
 * Comparator for sorting bets by date (newest first).
 */
public class DateBetComparator implements BetComparator {
    @Override
    public int compare(final Bet b1, final Bet b2) {
        return b2.getUpdatedAt().compareTo(b1.getUpdatedAt());
    }
}
