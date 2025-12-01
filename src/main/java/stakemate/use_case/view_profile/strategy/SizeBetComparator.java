package stakemate.use_case.view_profile.strategy;

import stakemate.use_case.settle_market.Bet;

/**
 * Comparator for sorting bets by size (largest first).
 */
public class SizeBetComparator implements BetComparator {
    @Override
    public int compare(final Bet b1, final Bet b2) {
        // For open bets: amount * price
        // For historical bets: amount * (won ? 1 : 0)
        // Since we don't know if it's open or historical here easily without context,
        // we can use a unified metric or just stake * price for now as a simple size
        // metric.
        // However, the original logic had different logic for open vs historical.
        // To keep it simple and consistent with the Interface Segregation Principle,
        // we might want separate comparators or a smarter one.
        // Let's stick to the original logic:
        // Open: stake * price
        // Historical: stake * (won ? 1 : 0)

        // But wait, the Strategy pattern usually applies to the same type of object.
        // If the logic differs based on state (settled vs not), the comparator should
        // handle it.

        double v1 = getValue(b1);
        double v2 = getValue(b2);
        return Double.compare(v2, v1);
    }

    private double getValue(final Bet bet) {
        if (Boolean.TRUE.equals(bet.isSettled())) {
            return bet.getStake() * (Boolean.TRUE.equals(bet.isWon()) ? 1.0 : 0.0);
        } else {
            return bet.getStake() * bet.getPrice();
        }
    }
}
