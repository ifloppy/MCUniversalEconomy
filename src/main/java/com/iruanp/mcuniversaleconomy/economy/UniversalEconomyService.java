package com.iruanp.mcuniversaleconomy.economy;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.Map;

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
     * Get the top balances with player names
     * @param limit The maximum number of entries to return
     * @return A CompletableFuture containing a list of entries with player names and balances
     */
    CompletableFuture<List<Map.Entry<String, BigDecimal>>> getTopBalances(int limit);

    /**
     * Format the amount according to the currency format
     */
    String format(BigDecimal amount);

    /**
     * Get a player's UUID by their username
     * @param username The player's username
     * @return A CompletableFuture containing the player's UUID, or null if not found
     */
    CompletableFuture<UUID> getUuidByUsername(String username);
} 