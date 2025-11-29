package stakemate.use_case.view_profile.strategy;

import java.util.Comparator;
import stakemate.use_case.settle_market.Bet;

/**
 * Strategy interface for comparing bets.
 */
public interface BetComparator extends Comparator<Bet> {
}
