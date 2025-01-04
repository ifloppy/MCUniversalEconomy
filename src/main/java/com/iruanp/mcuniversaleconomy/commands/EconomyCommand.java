package com.iruanp.mcuniversaleconomy.commands;

import com.iruanp.mcuniversaleconomy.economy.UniversalEconomyService;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EconomyCommand {
    private final UniversalEconomyService economyService;

    public EconomyCommand(UniversalEconomyService economyService) {
        this.economyService = economyService;
    }

    public CompletableFuture<String> balance(UUID playerUuid) {
        return economyService.getBalance(playerUuid)
            .thenApply(balance -> String.format("Your balance is %s", economyService.format(balance)));
    }

    public CompletableFuture<String> pay(UUID sourceUuid, UUID targetUuid, double amount) {
        if (amount <= 0) {
            return CompletableFuture.completedFuture("Amount must be positive");
        }

        BigDecimal decimalAmount = BigDecimal.valueOf(amount);
        return economyService.transfer(sourceUuid, targetUuid, decimalAmount)
            .thenApply(result -> result.isSuccess() ? 
                String.format("Successfully paid %s", result.getMessage()) :
                String.format("Failed to make payment: %s", result.getMessage()));
    }

    public CompletableFuture<String> give(UUID playerUuid, double amount) {
        if (amount <= 0) {
            return CompletableFuture.completedFuture("Amount must be positive");
        }

        BigDecimal decimalAmount = BigDecimal.valueOf(amount);
        return economyService.addBalance(playerUuid, decimalAmount)
            .thenApply(success -> success ?
                String.format("Successfully gave %s", economyService.format(decimalAmount)) :
                "Failed to give money");
    }

    public CompletableFuture<String> take(UUID playerUuid, double amount) {
        if (amount <= 0) {
            return CompletableFuture.completedFuture("Amount must be positive");
        }

        BigDecimal decimalAmount = BigDecimal.valueOf(amount);
        return economyService.subtractBalance(playerUuid, decimalAmount)
            .thenApply(success -> success ?
                String.format("Successfully took %s", economyService.format(decimalAmount)) :
                "Failed to take money");
    }

    public CompletableFuture<String> set(UUID playerUuid, double amount) {
        if (amount < 0) {
            return CompletableFuture.completedFuture("Amount cannot be negative");
        }

        BigDecimal decimalAmount = BigDecimal.valueOf(amount);
        return economyService.setBalance(playerUuid, decimalAmount)
            .thenApply(success -> success ?
                String.format("Successfully set balance to %s", economyService.format(decimalAmount)) :
                "Failed to set balance");
    }
} 