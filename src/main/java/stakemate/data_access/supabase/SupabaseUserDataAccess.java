package stakemate.data_access.supabase;

import stakemate.entity.User;
import stakemate.use_case.login.LoginUserDataAccessInterface;
import stakemate.use_case.signup.SignupUserDataAccessInterface;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data access implementation that stores users in the Supabase "profiles" table.
 * <p>
 * Expected table:
 * <p>
 * create table public.profiles (
 * id uuid primary key default gen_random_uuid(),
 * username text unique not null,
 * balance int4 not null default 0,
 * updated_at timestamptz default now(),
 * password varchar not null
 * );
 */
public class SupabaseUserDataAccess
        implements SignupUserDataAccessInterface, LoginUserDataAccessInterface {

    private final SupabaseClientFactory factory;

    public SupabaseUserDataAccess(SupabaseClientFactory factory) {
        this.factory = factory;
    }

    // ========== SignupUserDataAccessInterface ==========

    @Override
    public boolean existsByUsername(String username) {
        final String sql =
                "SELECT 1 FROM public.profiles WHERE username = ? LIMIT 1";

        try (Connection conn = factory.createConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // true if any row exists
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error checking if user exists", e);
        }
    }

    @Override
    public void save(User user) {
        // We let Supabase/Postgres generate the UUID id and updated_at
        final String sql =
                "INSERT INTO public.profiles (username, password, balance) " +
                        "VALUES (?, ?, ?)";

        try (Connection conn = factory.createConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword()); // plain text for now
            ps.setInt(3, user.getBalance());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving user to Supabase", e);
        }
    }

    // ========== LoginUserDataAccessInterface ==========

    @Override
    public User getByUsername(String username) {
        final String sql =
                "SELECT username, password, balance " +
                        "FROM public.profiles WHERE username = ?";

        try (Connection conn = factory.createConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null; // no such user
                }

                String uname = rs.getString("username");
                String pwd = rs.getString("password");
                int balance = rs.getInt("balance");

                // match your actual User constructor
                return new User(uname, pwd, balance);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error loading user from Supabase", e);
        }
    }
}
