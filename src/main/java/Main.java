// package org.example; // Package name is still removed

// Import the official Java SQL libraries

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Main {
    private static final String DB_HOST = "aws-1-ca-central-1.pooler.supabase.com"; // <-- Check your Supabase dashboard for the correct region
    private static final String DB_PORT = "5432"; // <-- Port from your URI (use 6543 for the pooler)
    private static final String DB_USER = "postgres.huqjovbougtvwlqtxppo"; // <-- This is your pooler user
    private static final String DB_PASSWORD = "stakematedb"; // <-- Password

    public static void main(String[] args) {
        // 2. Build the JDBC connection string
        String jdbcUrl = "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/postgres";

        // 3. Connect and run the query
        // We use try-with-resources to automatically close the connection

        // --- NEW TEST QUERY (INSERT) ---
        // This query inserts a new row into the 'markets' table.
        // We'll add a 'category' as well, just to make it more complete.
        // Using System.currentTimeMillis() to make the name unique every time.
        String marketName = "Test Market " + System.currentTimeMillis();
        String sql = "INSERT INTO public.markets (name, category) " +
                "VALUES ('" + marketName + "', 'Test Category')";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            System.out.println("Successfully connected to Supabase!");
            System.out.println("Attempting to insert: " + marketName);

            // 4. Execute the insert
            // executeUpdate() is used for INSERT, UPDATE, or DELETE statements.
            // It returns the number of rows affected.
            int rowsAffected = stmt.executeUpdate(sql);

            // 5. Print the result
            System.out.println("--- Query Result (INSERT) ---");
            if (rowsAffected > 0) {
                System.out.println("Success! " + rowsAffected + " row(s) inserted.");
                System.out.println("Check your 'markets' table in the Supabase dashboard to see the new row.");
            } else {
                System.out.println("Insert failed. No rows were affected.");
            }
            System.out.println("-----------------------------");

        } catch (Exception e) {
            System.err.println("Connection or INSERT failed! Check your credentials and RLS policies.");
            System.err.println("Hint: Make sure RLS is disabled for 'markets' or a policy exists that allows INSERTS.");
            e.printStackTrace();
        }
    }
}