package com.iruanp.mcuniversaleconomy.database;

import com.iruanp.mcuniversaleconomy.config.ModConfig;
import com.iruanp.mcuniversaleconomy.util.UnifiedLogger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {
    private final HikariDataSource dataSource;
    private final String prefix;
    private final String serverId;
    private final UnifiedLogger logger;

    public DatabaseManager(ModConfig config) {
        this.prefix = config.getTablePrefix();
        this.serverId = config.getServerId();
        this.logger = config.getLogger();

        try {
            // Explicitly load the MariaDB driver
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            logger.severe("MariaDB JDBC driver not found: " + e.getMessage());
            throw new RuntimeException("MariaDB JDBC driver not found", e);
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
        hikariConfig.setJdbcUrl(String.format("jdbc:mariadb://%s:%d/%s", 
            config.getDatabaseHost(), 
            config.getDatabasePort(), 
            config.getDatabaseName()));
        hikariConfig.setUsername(config.getDatabaseUsername());
        hikariConfig.setPassword(config.getDatabasePassword());
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);

        dataSource = new HikariDataSource(hikariConfig);
        initializeTables();
    }

    public String getPrefix() {
        return prefix;
    }

    public String getServerId() {
        return serverId;
    }

    private void initializeTables() {
        // Create accounts table
        String createAccountsTable = String.format("""
            CREATE TABLE IF NOT EXISTS %saccounts (
                uuid VARCHAR(36) PRIMARY KEY,
                username VARCHAR(16) NOT NULL,
                balance DECIMAL(20,2) NOT NULL DEFAULT 0.00,
                last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
            """, prefix);

        // Create transactions table
        String createTransactionsTable = String.format("""
            CREATE TABLE IF NOT EXISTS %stransactions (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                source_uuid VARCHAR(36),
                target_uuid VARCHAR(36),
                amount DECIMAL(20,2) NOT NULL,
                tax DECIMAL(20,2) NOT NULL,
                type VARCHAR(16) NOT NULL,
                server_id VARCHAR(36),
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (source_uuid) REFERENCES %saccounts(uuid),
                FOREIGN KEY (target_uuid) REFERENCES %saccounts(uuid)
            )
            """, prefix, prefix, prefix);

        // Create notifications table
        String createNotificationsTable = String.format("""
            CREATE TABLE IF NOT EXISTS %snotifications (
                id INT AUTO_INCREMENT PRIMARY KEY,
                recipient_uuid VARCHAR(36) NOT NULL,
                message TEXT NOT NULL,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """, prefix);

        try (Connection conn = getConnection();
             PreparedStatement accountsStmt = conn.prepareStatement(createAccountsTable);
             PreparedStatement transactionsStmt = conn.prepareStatement(createTransactionsTable);
             PreparedStatement notificationsStmt = conn.prepareStatement(createNotificationsTable)) {

            accountsStmt.executeUpdate();
            transactionsStmt.executeUpdate();
            notificationsStmt.executeUpdate();
            logger.info("Database tables initialized successfully");
        } catch (SQLException e) {
            logger.severe("Failed to initialize database tables: " + e.getMessage());
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
} 