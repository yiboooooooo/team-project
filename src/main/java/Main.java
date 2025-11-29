// package org.example;

// Import the official Java SQL libraries

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Main {
    private static final String DB_HOST = "aws-1-ca-central-1.pooler.supabase.com";
    private static final String DB_PORT = "5432";
    private static final String DB_USER = "postgres.huqjovbougtvwlqtxppo";
    private static final String DB_PASSWORD = "stakematedb";

    public static void main(final String[] args) {
        // Launch the application
        stakemate.app.StakeMateApp.main(args);
    }
}
