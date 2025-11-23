package stakemate.data_access.supabase;

import stakemate.entity.User;
import stakemate.use_case.settle_market.AccountRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Database implementation of AccountRepository using Supabase.
 * Backed by the "profiles" table.
 */
public class SupabaseAccountDataAccess implements AccountRepository {

    private final SupabaseClientFactory factory;

    public SupabaseAccountDataAccess(SupabaseClientFactory factory) {
        this.factory = factory;
    }

    @Override
    public User findByUsername(String username) {
        final String sql =
            "SELECT username, balance, password " +
                "FROM public.profiles WHERE username = ?";

        try (Connection conn = factory.createConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;  // user not found
                }

                String uname = rs.getString("username");
                String pwd   = rs.getString("password");
                int balance  = rs.getInt("balance");

                return new User(uname, pwd, balance);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching user from profiles", e);
        }
    }

    @Override
    public void save(User user) {
        final String sql =
            "UPDATE public.profiles " +
                "SET balance = ?, updated_at = NOW() " +
                "WHERE username = ?";

        try (Connection conn = factory.createConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, user.getBalance());
            ps.setString(2, user.getUsername());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving updated user balance", e);
        }
    }
}
