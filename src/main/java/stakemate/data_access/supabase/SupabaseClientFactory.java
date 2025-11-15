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
    private static final String DEFAULT_PORT = "5432";
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
     */
    public SupabaseClientFactory(String host, String port, String database, 
                                String user, String password) {
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
        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", host, port, database);
        return DriverManager.getConnection(jdbcUrl, user, password);
    }
    
    /**
     * Gets environment variable or returns default value.
     */
    private String getEnvOrDefault(String envVar, String defaultValue) {
        String value = System.getenv(envVar);
        return value != null ? value : defaultValue;
    }
}

