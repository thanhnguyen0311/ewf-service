package com.danny.ewf_service.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

@Component
public class SQLExecutor {

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUser;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    @Value("${sql.folder:db}") // Default to 'db' folder if key is missing
    private String sqlFolder;

    public void executeAllSQLScripts() {
        try {
            // Load the directory containing SQL files from resources
            File sqlDir = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(sqlFolder)).toURI());

            // Get all .sql files in the directory
            File[] sqlFiles = sqlDir.listFiles((dir, name) -> name.endsWith(".sql"));
            if (sqlFiles == null || sqlFiles.length == 0) {
                System.out.println("No SQL files found in the directory: " + sqlFolder);
                return;
            }

            // Sort files alphabetically to execute in the correct order
            Arrays.sort(sqlFiles, Comparator.comparing(File::getName));

            // Connect to the database
            try (Connection connection = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword)) {
                System.out.println("Connected to the database successfully!");

                for (File sqlFile : sqlFiles) {
                    // Execute each SQL file
                    System.out.println("Executing: " + sqlFile.getName());
                    executeSQLFile(connection, sqlFile);
                }
                System.out.println("All SQL files executed successfully!");
            }

        } catch (Exception e) {
            System.err.println("Error while executing SQL scripts: " + e.getMessage());
        }
    }

    private void executeSQLFile(Connection connection, File sqlFile) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(sqlFolder + "/" + sqlFile.getName()))))) {
            StringBuilder sqlQuery = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comments and empty lines
                if (!line.trim().startsWith("--") && !line.trim().isEmpty()) {
                    sqlQuery.append(line.trim()).append(" ");
                }

                // Detect and execute statements ending with semicolon
                if (line.trim().endsWith(";")) {
                    executeSQL(connection, sqlQuery.toString());
                    sqlQuery.setLength(0); // Reset for the next query
                }
            }

            // Execute the last statement if it doesn’t end with ';'
            if (!sqlQuery.isEmpty()) {
                executeSQL(connection, sqlQuery.toString());
            }
        } catch (IOException e) {
            System.err.println("Failed to read file " + sqlFile.getName() + ": " + e.getMessage());
        }
    }

    private void executeSQL(Connection connection, String sql) {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            System.out.println("Executed: " + sql);
        } catch (SQLException e) {
            System.err.println("Failed to execute SQL: " + sql + " - Error: " + e.getMessage());
        }
    }

}