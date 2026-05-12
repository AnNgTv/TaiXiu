package com.anngtv.taixiu.database;

import com.anngtv.taixiu.TaiXiu;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private final TaiXiu plugin;
    private Connection connection;

    public DatabaseManager(TaiXiu plugin) {
        this.plugin = plugin;
    }

    public void init() {
        try {
            String url = "jdbc:sqlite:" + plugin.getDataFolder() + "/data.db";
            connection = DriverManager.getConnection(url);
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS session_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "d1 INTEGER," +
                "d2 INTEGER," +
                "d3 INTEGER," +
                "total INTEGER," +
                "result TEXT," +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void saveSession(int d1, int d2, int d3, int total, String result) {
        String sql = "INSERT INTO session_history (d1, d2, d3, total, result) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, d1);
            pstmt.setInt(2, d2);
            pstmt.setInt(3, d3);
            pstmt.setInt(4, total);
            pstmt.setString(5, result);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getHistory(int limit) {
        List<String> history = new ArrayList<>();
        String sql = "SELECT * FROM session_history ORDER BY id DESC LIMIT ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                history.add(String.format("#%d: %s (%d,%d,%d = %d)", 
                        rs.getInt("id"), rs.getString("result"), 
                        rs.getInt("d1"), rs.getInt("d2"), rs.getInt("d3"), rs.getInt("total")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
