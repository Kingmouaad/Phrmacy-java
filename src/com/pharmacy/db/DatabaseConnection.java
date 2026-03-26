package com.pharmacy.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton database connection manager for SQLite.
 * Provides a shared connection and initializes the schema on first use.
 */
public class DatabaseConnection {
    
    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            // Enable foreign keys (off by default in SQLite)
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            System.out.println("[DB] Connected to SQLite database: pharmacy.db");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] SQLite JDBC driver not found! Add sqlite-jdbc JAR to classpath.");
            throw new RuntimeException("SQLite driver not found", e);
        } catch (SQLException e) {
            System.err.println("[DB] Failed to connect to database: " + e.getMessage());
            throw new RuntimeException("Database connection failed", e);
        }
    }

    /**
     * Get the singleton instance.
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Get the active database connection.
     * Reconnects if the connection was closed.
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON");
                }
                System.out.println("[DB] Reconnected to database.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Reconnection failed: " + e.getMessage());
            throw new RuntimeException("Database reconnection failed", e);
        }
        return connection;
    }

    /**
     * Initialize the database by running schema.sql.
     * Creates all tables and inserts sample data.
     */
    public void initializeDatabase() {
        try {
            String schemaPath = "src/schema.sql";
            String sql;

            if (Files.exists(Paths.get(schemaPath))) {
                sql = new String(Files.readAllBytes(Paths.get(schemaPath)));
            } else {
                // Try alternate path
                schemaPath = "schema.sql";
                if (Files.exists(Paths.get(schemaPath))) {
                    sql = new String(Files.readAllBytes(Paths.get(schemaPath)));
                } else {
                    System.err.println("[DB] schema.sql not found!");
                    return;
                }
            }

            // Execute each statement separated by semicolons
            Connection conn = getConnection();
            try (Statement stmt = conn.createStatement()) {
                // Split by semicolons OUTSIDE of single quotes.
                // This is required because schema.sql has string literals containing semicolons
                // (e.g., interaction descriptions like "Minor interaction; monitor ...").
                List<String> statements = splitSqlStatements(sql);
                for (String s : statements) {
                    String trimmed = s.trim();
                    String exec = stripLeadingSqlLineComments(trimmed);
                    if (exec.isEmpty()) continue;
                    try {
                        stmt.execute(exec);
                    } catch (SQLException e) {
                        // Some DROP statements may fail depending on table existence.
                        System.err.println("[DB] Statement error: " + e.getMessage());
                    }
                }
            }

            System.out.println("[DB] Database initialized successfully from schema.sql");
        } catch (IOException e) {
            System.err.println("[DB] Error reading schema.sql: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[DB] Error initializing database: " + e.getMessage());
        }
    }

    /**
     * Check if the database has been initialized (tables exist).
     */
    public boolean isInitialized() {
        try {
            Connection conn = getConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT 1 FROM products LIMIT 1");
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Close the database connection.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
        }
        instance = null;
    }

    /**
     * Split SQL script into statements by ';' while respecting single-quoted strings.
     */
    private static List<String> splitSqlStatements(String script) {
        List<String> out = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;

        for (int i = 0; i < script.length(); i++) {
            char c = script.charAt(i);

            if (c == '\'') {
                // Handle escaped quotes: '' inside a string literal
                if (inSingleQuote && i + 1 < script.length() && script.charAt(i + 1) == '\'') {
                    current.append("''");
                    i++; // skip next quote
                    continue;
                }
                inSingleQuote = !inSingleQuote;
                current.append(c);
                continue;
            }

            if (c == ';' && !inSingleQuote) {
                out.add(current.toString());
                current.setLength(0);
                continue;
            }

            current.append(c);
        }

        if (current.length() > 0) {
            out.add(current.toString());
        }

        return out;
    }

    /**
     * Remove leading '-- ...' line comments from a SQL statement.
     * The splitter may include comment blocks before the real SQL up to the next semicolon.
     */
    private static String stripLeadingSqlLineComments(String s) {
        String working = s;
        while (true) {
            String wTrim = working.trim();
            if (!wTrim.startsWith("--")) return wTrim;
            int newlineIdx = wTrim.indexOf('\n');
            if (newlineIdx < 0) return "";
            // Remove up to (and including) the newline.
            working = wTrim.substring(newlineIdx + 1);
        }
    }
}
