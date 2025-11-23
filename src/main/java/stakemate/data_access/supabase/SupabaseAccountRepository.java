package stakemate.data_access.supabase;

import stakemate.entity.User;
import stakemate.use_case.settle_market.AccountRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * AccountRepository implementation backed by the Supabase "profiles" table.
 *
 * Table: public.profiles
 *   id uuid primary key
 *   username text unique not null
 *   balance int4 not null
 *   updated_at timestamptz
 *   password varchar not null
 */
public class SupabaseAccountRepository implements AccountRepository {

    private final SupabaseClientFactory factory;

    public SupabaseAccountRepository(SupabaseClientFactory factory) {
        this.factory = factory;
    }

    @Override
    public User findByUsername(String username) {
        final String sql = "SELECT username, password, balance " +
            "FROM public.profiles WHERE username = ?";

        try (Connection conn = factory.createConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null; // or throw if you prefer
                }

                String uname = rs.getString("username");
                String pwd = rs.getString("password");
                int balance = rs.getInt("balance");

                return new User(uname, pwd, balance);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error loading user from Supabase", e);
        }
    }

    @Override
    public void save(User user) {
        final String sql = "UPDATE public.profiles " +
            "SET balance = ?, updated_at = now() " +
            "WHERE username = ?";

        try (Connection conn = factory.createConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, user.getBalance());
            ps.setString(2, user.getUsername());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving user to Supabase", e);
        }
    }
}
