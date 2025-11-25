package stakemate.data_access.supabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import stakemate.entity.User;
import stakemate.use_case.login.LoginUserDataAccessInterface;
import stakemate.use_case.signup.SignupUserDataAccessInterface;

/**
 * Data access implementation that stores users in the Supabase "profiles"
 * table.
 *
 * <p>
 * Expected table:
 *
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
    implements SignupUserDataAccessInterface, LoginUserDataAccessInterface,
    stakemate.use_case.view_profile.ViewProfileUserDataAccessInterface  {
    private final SupabaseClientFactory factory;

    public SupabaseUserDataAccess(final SupabaseClientFactory factory) {
        this.factory = factory;
    }

    // ========== SignupUserDataAccessInterface ==========

    @Override
    public boolean existsByUsername(final String username) {
        final String sql = "SELECT 1 FROM public.profiles WHERE username = ? LIMIT 1";

        try (Connection conn = factory.createConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        }
        catch (final SQLException ex) {
            throw new RuntimeException("Error checking if user exists", ex);
        }
    }

    @Override
    public void save(final User user) {
        // We let Supabase/Postgres generate the UUID id and updated_at
        final String sql = "INSERT INTO public.profiles (username, password, balance) "
            + "VALUES (?, ?, ?)";

        try (Connection conn = factory.createConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setInt(3, user.getBalance());

            ps.executeUpdate();

        }
        catch (final SQLException ex) {
            throw new RuntimeException("Error saving user to Supabase", ex);
        }
    }

    // ========== LoginUserDataAccessInterface ==========

    @Override
    public User getByUsername(final String username) {
        final String sql = "SELECT username, password, balance "
            + "FROM public.profiles WHERE username = ?";

        User res;

        try (Connection conn = factory.createConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    res = null;
                }

                final String uname = rs.getString("username");
                final String pwd = rs.getString("password");
                final int balance = rs.getInt("balance");
                res = new User(uname, pwd, balance);
            }
            return res;

        }
        catch (final SQLException ex) {
            throw new RuntimeException("Error loading user from Supabase", ex);
        }
    }
}
