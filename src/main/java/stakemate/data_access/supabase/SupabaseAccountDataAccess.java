package stakemate.data_access.supabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import stakemate.entity.User;
import stakemate.use_case.settle_market.AccountRepository;

/**
 * Database implementation of AccountRepository using Supabase.
 * Backed by the "profiles" table.
 */
public class SupabaseAccountDataAccess implements AccountRepository {

    private final SupabaseClientFactory factory;

    /**
     * Constructs a new SupabaseAccountDataAccess.
     *
     * @param factory the factory to create database connections.
     */
    public SupabaseAccountDataAccess(final SupabaseClientFactory factory) {
        this.factory = factory;
    }

    @Override
    public User findByUsername(final String username) {
        final String sql =
            "SELECT username, balance, password "
                + "FROM public.profiles WHERE username = ?";
        User user = null;

        try (Connection conn = factory.createConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final String uname = rs.getString("username");
                    final String pwd = rs.getString("password");
                    final int balance = rs.getInt("balance");

                    user = new User(uname, pwd, balance);
                }
            }

        }
        catch (final SQLException err) {
            throw new RuntimeException("Error fetching user from profiles", err);
        }

        return user;
    }

    @Override
    public void save(final User user) {
        final String sql =
            "UPDATE public.profiles "
                + "SET balance = ?, updated_at = NOW() "
                + "WHERE username = ?";

        try (Connection conn = factory.createConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, user.getBalance());
            ps.setString(2, user.getUsername());
            ps.executeUpdate();

        }
        catch (final SQLException err) {
            throw new RuntimeException("Error saving updated user balance", err);
        }
    }
}
