package com.iruanp.mcuniversaleconomy.commands;

import com.iruanp.mcuniversaleconomy.economy.UniversalEconomyService;
import com.iruanp.mcuniversaleconomy.lang.LanguageManager;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.Map;

public class EconomyCommand {
    private final UniversalEconomyService economyService;
    private final LanguageManager languageManager;

    public EconomyCommand(UniversalEconomyService economyService, LanguageManager languageManager) {
        this.economyService = economyService;
        this.languageManager = languageManager;
    }

    public CompletableFuture<String> balance(UUID playerUuid) {
        return economyService.getBalance(playerUuid)
            .thenApply(balance -> languageManager.getMessage("balance.success", economyService.format(balance)));
    }

    public CompletableFuture<String> balanceTop(int limit) {
        return economyService.getTopBalances(limit)
            .thenApply(topBalances -> {
                StringBuilder message = new StringBuilder();
                message.append(languageManager.getMessage("balance.top_title")).append("\n");
                int rank = 1;
                for (Map.Entry<String, BigDecimal> entry : topBalances) {
                    String formattedBalance = economyService.format(entry.getValue());
                    message.append(String.format("#%d. %s - %s\n", 
                        rank++, 
                        entry.getKey(), 
                        formattedBalance));
                }
                return message.toString().trim();
            });
    }

    public CompletableFuture<String> pay(UUID sourceUuid, UUID targetUuid, double amount) {
        if (amount <= 0) {
            return CompletableFuture.completedFuture(languageManager.getMessage("pay.error.amount_positive"));
        }

        BigDecimal decimalAmount = BigDecimal.valueOf(amount);
        return economyService.transfer(sourceUuid, targetUuid, decimalAmount)
            .thenApply(result -> result.isSuccess() ? 
                languageManager.getMessage("pay.success", result.getMessage()) :
                languageManager.getMessage("pay.failed", result.getMessage()));
    }

    public CompletableFuture<String> give(UUID playerUuid, double amount) {
        if (amount <= 0) {
            return CompletableFuture.completedFuture(languageManager.getMessage("give.error.amount_positive"));
        }

        BigDecimal decimalAmount = BigDecimal.valueOf(amount);
        return economyService.addBalance(playerUuid, decimalAmount)
            .thenApply(success -> success ?
                languageManager.getMessage("give.success", economyService.format(decimalAmount)) :
                languageManager.getMessage("give.failed"));
    }

    public CompletableFuture<String> take(UUID playerUuid, double amount) {
        if (amount <= 0) {
            return CompletableFuture.completedFuture(languageManager.getMessage("take.error.amount_positive"));
        }

        BigDecimal decimalAmount = BigDecimal.valueOf(amount);
        return economyService.subtractBalance(playerUuid, decimalAmount)
            .thenApply(success -> success ?
                languageManager.getMessage("take.success", economyService.format(decimalAmount)) :
                languageManager.getMessage("take.failed"));
    }

    public CompletableFuture<String> set(UUID playerUuid, double amount) {
        if (amount < 0) {
            return CompletableFuture.completedFuture(languageManager.getMessage("set.error.amount_negative"));
        }

        BigDecimal decimalAmount = BigDecimal.valueOf(amount);
        return economyService.setBalance(playerUuid, decimalAmount)
            .thenApply(success -> success ?
                languageManager.getMessage("set.success", economyService.format(decimalAmount)) :
                languageManager.getMessage("set.failed"));
    }
} 