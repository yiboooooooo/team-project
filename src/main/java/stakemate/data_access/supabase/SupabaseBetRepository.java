package stakemate.data_access.supabase;

import stakemate.entity.Side;
import stakemate.use_case.settle_market.Bet;
import stakemate.use_case.settle_market.BetRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupabaseBetRepository implements BetRepository {

    private final SupabaseClientFactory factory;

    public SupabaseBetRepository(SupabaseClientFactory factory) {
        this.factory = factory;
    }

    @Override
    public List<Bet> findByMarketId(String marketId) {

        final String sql =
            "SELECT p.username, " +
                "       pos.market_id, " +
                "       pos.side, " +
                "       pos.amount, " +
                "       pos.price, " +
                "       pos.\"won?\" AS won_flag, " +        // won column
                "       pos.settled AS settled_flag " +      // settled column
                "FROM public.positions pos " +
                "JOIN public.profiles p ON pos.user_id = p.id " +
                "WHERE pos.market_id = ? AND pos.settled = false";  // only unsettled

        List<Bet> bets = new ArrayList<>();

        try (Connection conn = factory.createConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, marketId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    String username = rs.getString("username");
                    String mId = rs.getString("market_id");
                    Side side = Side.valueOf(rs.getString("side").toUpperCase());
                    double amount = rs.getDouble("amount");
                    double price = rs.getDouble("price");

                    // won? column can be NULL → Boolean
                    Boolean won = (Boolean) rs.getObject("won_flag");

                    // settled column (should always be false here)
                    Boolean settled = (Boolean) rs.getObject("settled_flag");

                    // MATCHES your 7-argument constructor
                    Bet bet = new Bet(username, mId, side, amount, price, won, settled);

                    bets.add(bet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error loading bets for market " + marketId, e);
        }

        return bets;
    }

    @Override
    public void save(Bet bet) {

        final String sql =
            "UPDATE public.positions " +
                "SET settled = true, " +
                "    \"won?\" = ? " +
                "WHERE market_id = ? " +
                "  AND user_id = (SELECT id FROM public.profiles WHERE username = ?)";

        try (Connection conn = factory.createConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, bet.isWon());        // Boolean (true/false/null)
            ps.setString(2, bet.getMarketId());
            ps.setString(3, bet.getUsername());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating bet settlement status", e);
        }
    }
}
