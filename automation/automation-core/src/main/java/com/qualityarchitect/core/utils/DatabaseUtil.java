package com.qualityarchitect.core.utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseUtil {

    private static final String DEFAULT_DB_URL = "jdbc:sqlite:claims_test.db";
    private static final String DB_URL = System.getenv().getOrDefault("DB_URL", DEFAULT_DB_URL);
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", "");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "");

    private DatabaseUtil() {
        // Utility class
    }

    public static Connection getConnection() throws SQLException {
        if (DB_USER.isEmpty() && DB_PASSWORD.isEmpty()) {
            return DriverManager.getConnection(DB_URL);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static List<Map<String, Object>> executeQuery(String sql) {
        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnLabel(i), rs.getObject(i));
                }
                results.add(row);
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseUtil] Query execution error: " + e.getMessage());
            throw new RuntimeException("Failed to execute database query: " + sql, e);
        }
        return results;
    }

    public static Object getSingleValue(String sql, String columnName) {
        List<Map<String, Object>> rows = executeQuery(sql);
        if (rows.isEmpty() || !rows.get(0).containsKey(columnName)) {
            return null;
        }
        return rows.get(0).get(columnName);
    }

    public static int executeUpdate(String sql) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("[DatabaseUtil] Update execution error: " + e.getMessage());
            throw new RuntimeException("Failed to execute database update: " + sql, e);
        }
    }

    public static int executePreparedStatement(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DatabaseUtil] Prepared statement error: " + e.getMessage());
            throw new RuntimeException("Failed to execute prepared statement: " + sql, e);
        }
    }
}
