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

public class UniversalEconomyServiceImpl implements UniversalEconomyService {
    private final DatabaseManager databaseManager;
    private final UnifiedLogger logger;
    private final String prefix;
    private final ModConfig config;
    private final LanguageManager languageManager;

    public UniversalEconomyServiceImpl(DatabaseManager databaseManager, UnifiedLogger logger, ModConfig config, LanguageManager languageManager) {
        this.databaseManager = databaseManager;
        this.logger = logger;
        this.prefix = config.getTablePrefix();
        this.config = config;
        this.languageManager = languageManager;
    }

    @Override
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

    @Override
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

    @Override
    public CompletableFuture<Boolean> addBalance(UUID playerUuid, BigDecimal amount) {
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
                                "INSERT INTO " + prefix + "transactions (source_uuid, target_uuid, amount, tax, type, server_id) VALUES (?, ?, ?, ?, 'ADD', ?)")) {
                                transactionStmt.setString(1, playerUuid.toString());
                                transactionStmt.setString(2, playerUuid.toString());
                                transactionStmt.setBigDecimal(3, amount);
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
                logError("Failed to add balance", e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> subtractBalance(UUID playerUuid, BigDecimal amount) {
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
                                "INSERT INTO " + prefix + "transactions (source_uuid, target_uuid, amount, tax, type, server_id) VALUES (?, ?, ?, ?, 'SUBTRACT', ?)")) {
                                transactionStmt.setString(1, playerUuid.toString());
                                transactionStmt.setString(2, playerUuid.toString());
                                transactionStmt.setBigDecimal(3, amount.negate());
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
                logError("Failed to subtract balance", e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
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
                            "INSERT INTO " + prefix + "transactions (source_uuid, target_uuid, amount, tax, type, server_id) VALUES (?, ?, ?, ?, 'TRANSFER', ?)")) {
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

    @Override
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

    @Override
    public CompletableFuture<Boolean> createAccount(UUID playerUuid, String username) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = databaseManager.getConnection()) {
                // First check if account exists
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
                        return true; // Account exists and was potentially updated
                    }
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
            } catch (SQLException e) {
                logError("Failed to create/update account", e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public String getCurrencyNameSingular() {
        return languageManager.getMessage("currency.name_singular");
    }

    @Override
    public String getCurrencyNamePlural() {
        return languageManager.getMessage("currency.name_plural");
    }

    @Override
    public String format(BigDecimal amount) {
        return config.getCurrencySymbol() + amount.setScale(2).toString();
    }

    private void logError(String message, Exception e) {
        logger.error(message, e);
    }
} 