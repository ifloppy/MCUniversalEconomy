package com.iruanp.mcuniversaleconomy.economy.fabric;

import com.iruanp.mcuniversaleconomy.economy.UniversalEconomyService;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import eu.pb4.common.economy.api.EconomyTransaction;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.math.BigDecimal;
import java.util.UUID;

public class CommonEconomyAccount implements EconomyAccount {
    private final UniversalEconomyService economyService;
    private final UUID playerUuid;
    private final String playerName;
    private final EconomyCurrency currency;
    private final EconomyProvider provider;
    private final Identifier id;
    private final CommonEconomyCurrency commonCurrency;

    public CommonEconomyAccount(UniversalEconomyService economyService, UUID playerUuid, String playerName, EconomyCurrency currency, EconomyProvider provider, Identifier id) {
        this.economyService = economyService;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.currency = currency;
        this.provider = provider;
        this.id = id;
        this.commonCurrency = (CommonEconomyCurrency) currency;
    }

    @Override
    public Text name() {
        return Text.literal(playerName);
    }

    @Override
    public UUID owner() {
        return playerUuid;
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public long balance() {
        BigDecimal balance = economyService.getBalance(playerUuid).join();
        return balance.multiply(BigDecimal.valueOf(commonCurrency.getDecimalMultiplier())).longValue();
    }

    @Override
    public void setBalance(long value) {
        BigDecimal decimalValue = BigDecimal.valueOf(value)
            .divide(BigDecimal.valueOf(commonCurrency.getDecimalMultiplier()));
        economyService.setBalance(playerUuid, decimalValue);
    }

    @Override
    public EconomyTransaction canIncreaseBalance(long value) {
        return new EconomyTransaction.Simple(true, Text.literal("Can increase balance"), 
            balance() + value, balance(), value, this);
    }

    @Override
    public EconomyTransaction canDecreaseBalance(long value) {
        long currentBalance = balance();
        boolean canDecrease = currentBalance >= value;
        return new EconomyTransaction.Simple(canDecrease, 
            canDecrease ? Text.literal("Can decrease balance") : Text.literal("Insufficient funds"),
            canDecrease ? currentBalance - value : currentBalance,
            currentBalance, -value, this);
    }

    @Override
    public EconomyProvider provider() {
        return provider;
    }

    @Override
    public EconomyCurrency currency() {
        return currency;
    }
} 