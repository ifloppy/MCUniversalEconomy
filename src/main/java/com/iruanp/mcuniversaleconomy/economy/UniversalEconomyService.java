package com.iruanp.mcuniversaleconomy.economy;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UniversalEconomyService {
    /**
     * Get the balance of a player
     */
    CompletableFuture<BigDecimal> getBalance(UUID playerUuid);

    /**
     * Set the balance of a player
     */
    CompletableFuture<Boolean> setBalance(UUID playerUuid, BigDecimal amount);

    /**
     * Add to the balance of a player
     */
    CompletableFuture<Boolean> addBalance(UUID playerUuid, BigDecimal amount);

    /**
     * Subtract from the balance of a player
     */
    CompletableFuture<Boolean> subtractBalance(UUID playerUuid, BigDecimal amount);

    /**
     * Transfer money from one player to another
     * Returns a TransactionResult containing success status and message
     */
    CompletableFuture<TransactionResult> transfer(UUID sourceUuid, UUID targetUuid, BigDecimal amount);

    /**
     * Check if a player has an account
     */
    CompletableFuture<Boolean> hasAccount(UUID playerUuid);

    /**
     * Create an account for a player
     */
    CompletableFuture<Boolean> createAccount(UUID playerUuid, String username);

    /**
     * Get the currency's name (singular)
     */
    String getCurrencyNameSingular();

    /**
     * Get the currency's name (plural)
     */
    String getCurrencyNamePlural();

    /**
     * Format the amount according to the currency format
     */
    String format(BigDecimal amount);
} 