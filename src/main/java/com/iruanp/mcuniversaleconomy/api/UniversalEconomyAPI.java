package com.iruanp.mcuniversaleconomy.api;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.math.BigDecimal;

/**
 * UniversalEconomyAPI provides a platform-independent interface for interacting with
 * MCUniversalEconomy's core functionality. This API can be used by both FabricMC mods
 * and PaperMC plugins.
 */
public interface UniversalEconomyAPI {
    /**
     * Gets the balance of a player.
     *
     * @param playerUUID The UUID of the player
     * @return The player's current balance
     */
    CompletableFuture<BigDecimal> getBalance(UUID playerUUID);

    /**
     * Sets the balance of a player to a specific amount.
     *
     * @param playerUUID The UUID of the player
     * @param amount The amount to set
     * @return true if successful, false otherwise
     */
    CompletableFuture<Boolean> setBalance(UUID playerUUID, double amount);

    /**
     * Adds an amount to a player's balance.
     *
     * @param playerUUID The UUID of the player
     * @param amount The amount to add
     * @return true if successful, false otherwise
     */
    CompletableFuture<Boolean> depositPlayer(UUID playerUUID, double amount);

    /**
     * Subtracts an amount from a player's balance.
     *
     * @param playerUUID The UUID of the player
     * @param amount The amount to subtract
     * @return true if successful, false otherwise
     */
    CompletableFuture<Boolean> withdrawPlayer(UUID playerUUID, double amount);

    /**
     * Transfers an amount from one player to another.
     *
     * @param fromUUID The UUID of the player sending money
     * @param toUUID The UUID of the player receiving money
     * @param amount The amount to transfer
     * @return A CompletableFuture containing the TransactionResult
     */
    CompletableFuture<com.iruanp.mcuniversaleconomy.economy.TransactionResult> transferMoney(UUID fromUUID, UUID toUUID, double amount);

    /**
     * Checks if a player has at least the specified amount.
     *
     * @param playerUUID The UUID of the player
     * @param amount The amount to check
     * @return true if the player has enough money, false otherwise
     */
    CompletableFuture<Boolean> hasEnough(UUID playerUUID, double amount);

    /**
     * Gets the currency symbol used by the economy.
     *
     * @return The currency symbol
     */
    String getCurrencySymbol();

    /**
     * Formats the amount according to the economy's settings.
     *
     * @param amount The amount to format
     * @return The formatted amount string
     */
    String formatAmount(double amount);
}