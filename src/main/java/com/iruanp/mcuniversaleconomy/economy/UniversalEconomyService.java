package com.iruanp.mcuniversaleconomy.economy;

import com.iruanp.mcuniversaleconomy.config.ModConfig;
import com.iruanp.mcuniversaleconomy.database.DatabaseManager;
import com.iruanp.mcuniversaleconomy.util.UnifiedLogger;
import com.iruanp.mcuniversaleconomy.lang.LanguageManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap;
import java.util.ArrayList;

public class UniversalEconomyService {
    private final DatabaseManager databaseManager;
    private final UnifiedLogger logger;
    private final String prefix;
    private final ModConfig config;
    private final LanguageManager languageManager;

    public UniversalEconomyService(DatabaseManager databaseManager, UnifiedLogger logger, ModConfig config, LanguageManager languageManager) {
        this.databaseManager = databaseManager;
        this.logger = logger;
        this.prefix = config.getTablePrefix();
        this.config = config;
        this.languageManager = languageManager;
    }

    public CompletableFuture<BigDecimal> getBalance(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT balance FROM " + prefix + "accounts WHERE uuid = ?")) {
                stmt.setString(1, playerUuid.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getBigDecimal("balance");
                }
                return BigDecimal.ZERO;
            } catch (SQLException e) {
                logError("Failed to get balance", e);
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Boolean> setBalance(UUID playerUuid, BigDecimal amount) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = databaseManager.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // Get old balance for transaction record
                    BigDecimal oldBalance;
                    try (PreparedStatement getStmt = conn.prepareStatement(
                        "SELECT balance FROM " + prefix + "accounts WHERE uuid = ?")) {
                        getStmt.setString(1, playerUuid.toString());
                        ResultSet rs = getStmt.executeQuery();
                        oldBalance = rs.next() ? rs.getBigDecimal("balance") : BigDecimal.ZERO;
                    }

                    // Set new balance
                    try (PreparedStatement setStmt = conn.prepareStatement(
                        "UPDATE " + prefix + "accounts SET balance = ? WHERE uuid = ?")) {
                        setStmt.setBigDecimal(1, amount);
                        setStmt.setString(2, playerUuid.toString());
                        boolean success = setStmt.executeUpdate() > 0;
                        
                        // Record transaction if enabled and successful
                        if (success && config.isEnableLogging()) {
                            BigDecimal difference = amount.subtract(oldBalance);
                            try (PreparedStatement transactionStmt = conn.prepareStatement(
                                "INSERT INTO " + prefix + "transactions (source_uuid, target_uuid, amount, tax, type, server_id) VALUES (?, ?, ?, ?, 'SET', ?)")) {
                                transactionStmt.setString(1, playerUuid.toString());
                                transactionStmt.setString(2, playerUuid.toString());
                                transactionStmt.setBigDecimal(3, difference);
                                transactionStmt.setBigDecimal(4, BigDecimal.ZERO);
                                transactionStmt.setString(5, databaseManager.getServerId());
                                transactionStmt.executeUpdate();
                            }
                        }
                        
                        conn.commit();
                        return success;
                    }
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                logError("Failed to set balance", e);
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Boolean> addBalance(UUID playerUuid, BigDecimal amount) {
        return addBalance(playerUuid, amount, false);
    }

    public CompletableFuture<Boolean> addBalance(UUID playerUuid, BigDecimal amount, boolean isCommandCall) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = databaseManager.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // Add balance
                    try (PreparedStatement addStmt = conn.prepareStatement(
                        "UPDATE " + prefix + "accounts SET balance = balance + ? WHERE uuid = ?")) {
                        addStmt.setBigDecimal(1, amount);
                        addStmt.setString(2, playerUuid.toString());
                        boolean success = addStmt.executeUpdate() > 0;

                        // Record transaction if enabled and successful
                        if (success && config.isEnableLogging()) {
                            try (PreparedStatement transactionStmt = conn.prepareStatement(
                                "INSERT INTO " + prefix + "transactions (source_uuid, target_uuid, amount, tax, type, server_id) VALUES (?, ?, ?, ?, ?, ?)")) {
                                transactionStmt.setString(1, playerUuid.toString());
                                transactionStmt.setString(2, playerUuid.toString());
                                transactionStmt.setBigDecimal(3, amount);
                                transactionStmt.setBigDecimal(4, BigDecimal.ZERO);
                                transactionStmt.setString(5, isCommandCall ? "GIVE" : "INC");
                                transactionStmt.setString(6, databaseManager.getServerId());
                                transactionStmt.executeUpdate();
                            }
                        }

                        conn.commit();
                        return success;
                    }
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                logError("Failed to add balance", e);
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Boolean> subtractBalance(UUID playerUuid, BigDecimal amount) {
        return subtractBalance(playerUuid, amount, false);
    }

    public CompletableFuture<Boolean> subtractBalance(UUID playerUuid, BigDecimal amount, boolean isCommandCall) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = databaseManager.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // Check if they have enough
                    try (PreparedStatement checkStmt = conn.prepareStatement(
                        "SELECT balance FROM " + prefix + "accounts WHERE uuid = ? AND balance >= ?")) {
                        checkStmt.setString(1, playerUuid.toString());
                        checkStmt.setBigDecimal(2, amount);
                        if (!checkStmt.executeQuery().next()) {
                            conn.rollback();
                            return false;
                        }
                    }

                    // Subtract the amount
                    try (PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE " + prefix + "accounts SET balance = balance - ? WHERE uuid = ?")) {
                        updateStmt.setBigDecimal(1, amount);
                        updateStmt.setString(2, playerUuid.toString());
                        boolean success = updateStmt.executeUpdate() > 0;

                        // Record transaction if enabled and successful
                        if (success && config.isEnableLogging()) {
                            try (PreparedStatement transactionStmt = conn.prepareStatement(
                                "INSERT INTO " + prefix + "transactions (source_uuid, target_uuid, amount, tax, type, server_id) VALUES (?, ?, ?, ?, ?, ?)")) {
                                transactionStmt.setString(1, playerUuid.toString());
                                transactionStmt.setString(2, playerUuid.toString());
                                transactionStmt.setBigDecimal(3, amount.negate());
                                transactionStmt.setBigDecimal(4, BigDecimal.ZERO);
                                transactionStmt.setString(5, isCommandCall ? "TAKE" : "DEC");
                                transactionStmt.setString(6, databaseManager.getServerId());
                                transactionStmt.executeUpdate();
                            }
                        }

                        conn.commit();
                        return success;
                    }
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                logError("Failed to subtract balance", e);
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<TransactionResult> transfer(UUID sourceUuid, UUID targetUuid, BigDecimal amount) {
        return CompletableFuture.supplyAsync(() -> {
            // Check minimum payment
            if (config.getMinimumPayment() > 0 && amount.doubleValue() < config.getMinimumPayment()) {
                return new TransactionResult(false, "Amount is below minimum payment of " + format(BigDecimal.valueOf(config.getMinimumPayment())));
            }

            // Check maximum payment
            if (config.getMaximumPayment() > 0 && amount.doubleValue() > config.getMaximumPayment()) {
                return new TransactionResult(false, "Amount is above maximum payment of " + format(BigDecimal.valueOf(config.getMaximumPayment())));
            }

            // Calculate tax
            BigDecimal tax = amount.multiply(BigDecimal.valueOf(config.getPaymentTax()));
            BigDecimal totalDeduction = amount.add(tax);

            try (Connection conn = databaseManager.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // Check if source has enough including tax
                    try (PreparedStatement checkStmt = conn.prepareStatement(
                        "SELECT balance FROM " + prefix + "accounts WHERE uuid = ? AND balance >= ?")) {
                        checkStmt.setString(1, sourceUuid.toString());
                        checkStmt.setBigDecimal(2, totalDeduction);
                        if (!checkStmt.executeQuery().next()) {
                            conn.rollback();
                            return new TransactionResult(false, "Insufficient funds (including tax of " + format(tax) + ")");
                        }
                    }

                    // Subtract from source (amount + tax)
                    try (PreparedStatement sourceStmt = conn.prepareStatement(
                        "UPDATE " + prefix + "accounts SET balance = balance - ? WHERE uuid = ?")) {
                        sourceStmt.setBigDecimal(1, totalDeduction);
                        sourceStmt.setString(2, sourceUuid.toString());
                        sourceStmt.executeUpdate();
                    }

                    // Add to target (amount only, tax is burned)
                    try (PreparedStatement targetStmt = conn.prepareStatement(
                        "UPDATE " + prefix + "accounts SET balance = balance + ? WHERE uuid = ?")) {
                        targetStmt.setBigDecimal(1, amount);
                        targetStmt.setString(2, targetUuid.toString());
                        targetStmt.executeUpdate();
                    }

                    // Record transaction if enabled
                    if (config.isEnableLogging()) {
                        try (PreparedStatement transactionStmt = conn.prepareStatement(
                            "INSERT INTO " + prefix + "transactions (source_uuid, target_uuid, amount, tax, type, server_id) VALUES (?, ?, ?, ?, 'PAY', ?)")) {
                            transactionStmt.setString(1, sourceUuid.toString());
                            transactionStmt.setString(2, targetUuid.toString());
                            transactionStmt.setBigDecimal(3, amount);
                            transactionStmt.setBigDecimal(4, tax);
                            transactionStmt.setString(5, databaseManager.getServerId());
                            transactionStmt.executeUpdate();
                        }
                    }

                    conn.commit();
                    return new TransactionResult(true, "Successfully transferred " + format(amount) + " (including tax of " + format(tax) + ")");
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                logError("Failed to transfer", e);
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Boolean> hasAccount(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT 1 FROM " + prefix + "accounts WHERE uuid = ?")) {
                stmt.setString(1, playerUuid.toString());
                return stmt.executeQuery().next();
            } catch (SQLException e) {
                logError("Failed to check account", e);
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Boolean> createAccount(UUID playerUuid, String username) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = databaseManager.getConnection()) {
                // First check if account exists and get current username
                try (PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT username FROM " + prefix + "accounts WHERE uuid = ?")) {
                    checkStmt.setString(1, playerUuid.toString());
                    ResultSet rs = checkStmt.executeQuery();
                    
                    if (rs.next()) {
                        String existingUsername = rs.getString("username");
                        // Update username if it changed
                        if (!existingUsername.equals(username)) {
                            try (PreparedStatement updateStmt = conn.prepareStatement(
                                "UPDATE " + prefix + "accounts SET username = ? WHERE uuid = ?")) {
                                updateStmt.setString(1, username);
                                updateStmt.setString(2, playerUuid.toString());
                                updateStmt.executeUpdate();
                                logger.info("Updated username for existing account: " + existingUsername + " -> " + username);
                            }
                        }
                        return true;
                    }

                    // Create new account if it doesn't exist
                    try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO " + prefix + "accounts (uuid, username, balance) VALUES (?, ?, 0)")) {
                        insertStmt.setString(1, playerUuid.toString());
                        insertStmt.setString(2, username);
                        insertStmt.executeUpdate();
                        logger.info("Created new account for player: " + username);
                        return true;
                    }
                }
            } catch (SQLException e) {
                logError("Failed to create/update account", e);
                throw new RuntimeException(e);
            }
        });
    }

    public String getCurrencyNameSingular() {
        return languageManager.getMessage("currency.name_singular");
    }

    public String getCurrencyNamePlural() {
        return languageManager.getMessage("currency.name_plural");
    }

    public String format(BigDecimal amount) {
        return amount.toPlainString() + " " + (amount.compareTo(BigDecimal.ONE) == 0 ? getCurrencyNameSingular() : getCurrencyNamePlural());
    }

    public CompletableFuture<List<Map.Entry<String, BigDecimal>>> getTopBalances(int limit) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT username, balance FROM " + prefix + "accounts ORDER BY balance DESC LIMIT ?")) {
                stmt.setInt(1, limit);
                ResultSet rs = stmt.executeQuery();
                List<Map.Entry<String, BigDecimal>> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(new AbstractMap.SimpleEntry<>(
                        rs.getString("username"),
                        rs.getBigDecimal("balance")
                    ));
                }
                return results;
            } catch (SQLException e) {
                logError("Failed to get top balances", e);
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<UUID> getUuidByUsername(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT uuid FROM " + prefix + "accounts WHERE LOWER(username) = LOWER(?)")) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return UUID.fromString(rs.getString("uuid"));
                }
                return null;
            } catch (SQLException e) {
                logError("Failed to get UUID for username", e);
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<BigDecimal> getTotalBalance() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT SUM(balance) as total FROM " + prefix + "accounts")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getBigDecimal("total");
                }
                return BigDecimal.ZERO;
            } catch (SQLException e) {
                logError("Failed to get total balance", e);
                throw new RuntimeException(e);
            }
        });
    }

    private void logError(String message, Exception e) {
        logger.error(message, e);
    }
} 