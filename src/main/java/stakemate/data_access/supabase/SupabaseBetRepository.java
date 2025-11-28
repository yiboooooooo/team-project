package stakemate.data_access.supabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import stakemate.entity.Side;
import stakemate.use_case.settle_market.Bet;
import stakemate.use_case.settle_market.BetRepository;

/**
 * Implementation of BetRepository using Supabase/PostgreSQL.
 */
public class SupabaseBetRepository implements BetRepository {

    private final SupabaseClientFactory factory;

    public SupabaseBetRepository(final SupabaseClientFactory factory) {
        this.factory = factory;
    }

    @Override
    public List<Bet> findByMarketId(final String marketId) {

        final String sql =
            "SELECT p.username, "
                + "       pos.market_id, "
                + "       pos.side, "
                + "       pos.amount, "
                + "       pos.price, "
                + "       pos.\"won?\" AS won_flag, "
                + "       pos.settled AS settled_flag "
                + "FROM public.positions pos "
                + "JOIN public.profiles p ON pos.user_id = p.id "
                + "WHERE pos.market_id = ? AND pos.settled = false";

        final List<Bet> bets = new ArrayList<>();

        try (Connection conn = factory.createConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, marketId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    final String username = rs.getString("username");
                    final String mId = rs.getString("market_id");
                    final Side side = Side.valueOf(rs.getString("side").toUpperCase());
                    final double amount = rs.getDouble("amount");
                    final double price = rs.getDouble("price");

                    // won? column can be NULL -> Boolean
                    final Boolean won = (Boolean) rs.getObject("won_flag");

                    // settled column (should always be false here)
                    final Boolean settled = (Boolean) rs.getObject("settled_flag");

                    // MATCHES your 7-argument constructor
                    final Bet bet = new Bet(username, mId, side, amount, price, won, settled);

                    bets.add(bet);
                }
            }

        }
        catch (final SQLException err) {
            throw new RuntimeException("Error loading bets for market " + marketId, err);
        }

        return bets;
    }

    @Override
    public void save(final Bet bet) {

        final String sql =
            "UPDATE public.positions "
                + "SET settled = true, "
                + "    \"won?\" = ? "
                + "WHERE market_id = ? "
                + "  AND user_id = (SELECT id FROM public.profiles WHERE username = ?)";

        try (Connection conn = factory.createConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, bet.isWon());
            ps.setString(2, bet.getMarketId());
            ps.setString(3, bet.getUsername());

            ps.executeUpdate();

        }
        catch (final SQLException err) {
            throw new RuntimeException("Error updating bet settlement status", err);
        }
    }
}
