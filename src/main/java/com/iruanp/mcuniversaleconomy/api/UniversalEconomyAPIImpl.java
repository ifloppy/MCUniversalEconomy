package com.iruanp.mcuniversaleconomy.api;

import com.iruanp.mcuniversaleconomy.economy.TransactionResult;
import com.iruanp.mcuniversaleconomy.economy.UniversalEconomyService;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.math.BigDecimal;

/**
 * Implementation of the UniversalEconomyAPI interface that delegates operations to
 * the UniversalEconomyService.
 */
public class UniversalEconomyAPIImpl implements UniversalEconomyAPI {
    private final UniversalEconomyService economyService;

    public UniversalEconomyAPIImpl(UniversalEconomyService economyService) {
        this.economyService = economyService;
    }

    @Override
    public CompletableFuture<BigDecimal> getBalance(UUID playerUUID) {
        return economyService.getBalance(playerUUID);
    }

    @Override
    public CompletableFuture<Boolean> setBalance(UUID playerUUID, double amount) {
        return economyService.setBalance(playerUUID, BigDecimal.valueOf(amount));
    }

    @Override
    public CompletableFuture<Boolean> depositPlayer(UUID playerUUID, double amount) {
        return economyService.addBalance(playerUUID, BigDecimal.valueOf(amount));
    }

    @Override
    public CompletableFuture<Boolean> withdrawPlayer(UUID playerUUID, double amount) {
        return economyService.subtractBalance(playerUUID, BigDecimal.valueOf(amount));
    }

    @Override
    public CompletableFuture<TransactionResult> transferMoney(UUID fromUUID, UUID toUUID, double amount) {
        return economyService.transfer(fromUUID, toUUID, BigDecimal.valueOf(amount));
    }

    @Override
    public CompletableFuture<Boolean> hasEnough(UUID playerUUID, double amount) {
        return getBalance(playerUUID)
            .thenApply(balance -> balance.compareTo(BigDecimal.valueOf(amount)) >= 0);
    }

    @Override
    public String getCurrencySymbol() {
        return economyService.getConfig().getCurrencySymbol();
    }

    @Override
    public String formatAmount(double amount) {
        String format = economyService.getConfig().getCurrencyFormat();
        java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat(format);
        return getCurrencySymbol() + decimalFormat.format(amount);
    }

    /**
     * Gets the singleton instance of the UniversalEconomyAPI.
     *
     * @return The UniversalEconomyAPI instance
     */
    public static UniversalEconomyAPI getInstance() {
        return UniversalEconomyAPIHolder.INSTANCE;
    }

    /**
     * Internal holder class for the singleton instance.
     */
    private static class UniversalEconomyAPIHolder {
        private static final UniversalEconomyAPI INSTANCE = new UniversalEconomyAPIImpl(
            UniversalEconomyService.getInstance()
        );
    }
}