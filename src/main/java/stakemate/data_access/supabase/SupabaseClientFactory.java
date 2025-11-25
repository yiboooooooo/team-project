package stakemate.data_access.supabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Factory for creating Supabase database connections.
 * Manages connection configuration and provides reusable connections.
 */
public class SupabaseClientFactory {

    // Default values (can be overridden by environment variables)
    private static final String DEFAULT_HOST = "aws-1-ca-central-1.pooler.supabase.com";
    private static final String DEFAULT_PORT = "6543";
    private static final String DEFAULT_DATABASE = "postgres";

    private final String host;
    private final String port;
    private final String database;
    private final String user;
    private final String password;

    /**
     * Creates a factory using environment variables or defaults.
     * Environment variables:
     * - SUPABASE_DB_HOST
     * - SUPABASE_DB_PORT
     * - SUPABASE_DB_NAME
     * - SUPABASE_DB_USER
     * - SUPABASE_DB_PASSWORD
     */
    public SupabaseClientFactory() {
        this.host = getEnvOrDefault("SUPABASE_DB_HOST", DEFAULT_HOST);
        this.port = getEnvOrDefault("SUPABASE_DB_PORT", DEFAULT_PORT);
        this.database = getEnvOrDefault("SUPABASE_DB_NAME", DEFAULT_DATABASE);
        this.user = getEnvOrDefault("SUPABASE_DB_USER", "postgres.huqjovbougtvwlqtxppo");
        this.password = getEnvOrDefault("SUPABASE_DB_PASSWORD", "stakematedb");
    }

    /**
     * Creates a factory with explicit credentials.
     * @param host the database host address
     * @param port the database port number
     * @param database the database name
     * @param user the database username
     * @param password the database password
     */
    public SupabaseClientFactory(final String host, final String port, final String database,
                                 final String user, final String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
    }

    /**
     * Creates a new database connection.
     *
     * @return A new Connection to the Supabase database
     * @throws SQLException if connection fails
     */
    public Connection createConnection() throws SQLException {
        System.out.println("[SupabaseClientFactory] Opening new database connection...");
        final String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s?\n"
                + "prepareThreshold=0&preparedStatementCacheQueries=0",
            host, port, database);
        return DriverManager.getConnection(jdbcUrl, user, password);
    }

    /**
     * Gets environment variable or returns default value.
     * Checks both system environment and system properties (.env file support).
     * @param envVar the environment variable set in the .env
     * @param defaultValue the default variable to use in place of the envVar
     * @return the .env value if it exists, default value if it doesn't
     */
    private String getEnvOrDefault(final String envVar, final String defaultValue) {
        String value = System.getenv(envVar);
        final String res;
        if (value == null) {
            value = System.getProperty(envVar);
        }
        if (value != null) {
            res = value;
        }
        else {
            res = defaultValue;
        }
        return res;
    }
}

