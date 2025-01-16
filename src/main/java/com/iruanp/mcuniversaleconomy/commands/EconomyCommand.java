package com.iruanp.mcuniversaleconomy.commands;

import com.iruanp.mcuniversaleconomy.economy.UniversalEconomyService;
import com.iruanp.mcuniversaleconomy.lang.LanguageManager;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.Map;
import java.util.List;
import com.iruanp.mcuniversaleconomy.notification.NotificationService;

public class EconomyCommand {
    private final UniversalEconomyService economyService;
    private final LanguageManager languageManager;
    private final NotificationService notificationService;

    public EconomyCommand(UniversalEconomyService economyService, LanguageManager languageManager, NotificationService notificationService) {
        this.economyService = economyService;
        this.languageManager = languageManager;
        this.notificationService = notificationService;
    }

    public CompletableFuture<String> balance(UUID playerUuid) {
        return economyService.getBalance(playerUuid)
            .thenApply(balance -> languageManager.getMessage("balance.success", economyService.format(balance)));
    }

    public CompletableFuture<String> balanceTop(int limit) {
        CompletableFuture<List<Map.Entry<String, BigDecimal>>> topBalancesFuture = economyService.getTopBalances(limit);
        CompletableFuture<BigDecimal> totalBalanceFuture = economyService.getTotalBalance();

        return CompletableFuture.allOf(topBalancesFuture, totalBalanceFuture)
            .thenApply(v -> {
                List<Map.Entry<String, BigDecimal>> topBalances = topBalancesFuture.join();
                BigDecimal totalEconomyBalance = totalBalanceFuture.join();

                StringBuilder message = new StringBuilder();
                message.append(languageManager.getMessage("balance.top_title")).append("\n");
                
                int rank = 1;
                BigDecimal topTotalBalance = BigDecimal.ZERO;
                for (Map.Entry<String, BigDecimal> entry : topBalances) {
                    String formattedBalance = economyService.format(entry.getValue());
                    message.append(String.format("#%d. %s - %s\n", 
                        rank++, 
                        entry.getKey(), 
                        formattedBalance));
                    topTotalBalance = topTotalBalance.add(entry.getValue());
                }
                
                message.append("\n")
                    .append(languageManager.getMessage("balance.total_balance", economyService.format(topTotalBalance)))
                    .append("\n")
                    .append(languageManager.getMessage("balance.economy_total", economyService.format(totalEconomyBalance)));
                
                return message.toString().trim();
            });
    }

    public CompletableFuture<String> pay(UUID sourceUuid, UUID targetUuid, double amount) {
        if (amount <= 0) {
            return CompletableFuture.completedFuture(languageManager.getMessage("pay.error.amount_positive"));
        }

        BigDecimal decimalAmount = BigDecimal.valueOf(amount);
        return economyService.transfer(sourceUuid, targetUuid, decimalAmount)
            .thenApply(result -> {
                if (result.isSuccess()) {
                    String message = languageManager.getMessage("pay.success", result.getMessage());
                    notificationService.sendNotification(targetUuid, message);
                    return message;
                } else {
                    return languageManager.getMessage("pay.failed", result.getMessage());
                }
            });
    }

    public CompletableFuture<String> give(UUID playerUuid, double amount) {
        if (amount <= 0) {
            return CompletableFuture.completedFuture(languageManager.getMessage("give.error.amount_positive"));
        }

        BigDecimal decimalAmount = BigDecimal.valueOf(amount);
        return economyService.addBalance(playerUuid, decimalAmount, true)
            .thenApply(success -> success ?
                languageManager.getMessage("give.success", economyService.format(decimalAmount)) :
                languageManager.getMessage("give.failed"));
    }

    public CompletableFuture<String> take(UUID playerUuid, double amount) {
        if (amount <= 0) {
            return CompletableFuture.completedFuture(languageManager.getMessage("take.error.amount_positive"));
        }

        BigDecimal decimalAmount = BigDecimal.valueOf(amount);
        return economyService.subtractBalance(playerUuid, decimalAmount, true)
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