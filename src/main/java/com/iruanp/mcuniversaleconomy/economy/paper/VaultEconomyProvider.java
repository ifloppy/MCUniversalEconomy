package com.iruanp.mcuniversaleconomy.economy.paper;

import com.iruanp.mcuniversaleconomy.MCUniversalEconomyPaper;
import com.iruanp.mcuniversaleconomy.economy.UniversalEconomyService;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class VaultEconomyProvider implements Economy {
    private final UniversalEconomyService economyService;
    private final MCUniversalEconomyPaper plugin;
    private final String name = "MCUniversalEconomy";

    public VaultEconomyProvider(UniversalEconomyService economyService, MCUniversalEconomyPaper plugin) {
        this.economyService = economyService;
        this.plugin = plugin;
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String format(double amount) {
        return economyService.format(BigDecimal.valueOf(amount));
    }

    @Override
    public String currencyNamePlural() {
        return economyService.getCurrencyNamePlural();
    }

    @Override
    public String currencyNameSingular() {
        return economyService.getCurrencyNameSingular();
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return economyService.hasAccount(player.getUniqueId()).join();
    }

    @Override
    public boolean hasAccount(String playerName) {
        return hasAccount(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return economyService.getBalance(player.getUniqueId()).join().doubleValue();
    }

    @Override
    public double getBalance(String playerName) {
        return getBalance(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public boolean has(String playerName, double amount) {
        return has(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        boolean success = economyService.subtractBalance(player.getUniqueId(), BigDecimal.valueOf(amount)).join();
        return new EconomyResponse(amount, getBalance(player), 
            success ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE,
            success ? null : "Failed to withdraw amount");
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return withdrawPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        boolean success = economyService.addBalance(player.getUniqueId(), BigDecimal.valueOf(amount)).join();
        return new EconomyResponse(amount, getBalance(player),
            success ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE,
            success ? null : "Failed to deposit amount");
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return depositPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return economyService.createAccount(player.getUniqueId(), player.getName()).join();
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return createPlayerAccount(Bukkit.getOfflinePlayer(playerName));
    }

    // Required method implementations that we don't use
    @Override
    public int fractionalDigits() { return 2; }
    @Override
    public boolean hasAccount(String playerName, String worldName) { return hasAccount(playerName); }
    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) { return hasAccount(player); }
    @Override
    public double getBalance(String playerName, String world) { return getBalance(playerName); }
    @Override
    public double getBalance(OfflinePlayer player, String world) { return getBalance(player); }
    @Override
    public boolean has(String playerName, String worldName, double amount) { return has(playerName, amount); }
    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) { return has(player, amount); }
    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) { return withdrawPlayer(playerName, amount); }
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) { return withdrawPlayer(player, amount); }
    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) { return depositPlayer(playerName, amount); }
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) { return depositPlayer(player, amount); }
    @Override
    public boolean createPlayerAccount(String playerName, String worldName) { return createPlayerAccount(playerName); }
    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) { return createPlayerAccount(player); }

    @Override
    public boolean hasBankSupport() { return false; }

    // Bank methods - Not supported
    @Override
    public EconomyResponse createBank(String name, String player) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported"); }
    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported"); }
    @Override
    public EconomyResponse deleteBank(String name) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported"); }
    @Override
    public EconomyResponse bankBalance(String name) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported"); }
    @Override
    public EconomyResponse bankHas(String name, double amount) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported"); }
    @Override
    public EconomyResponse bankWithdraw(String name, double amount) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported"); }
    @Override
    public EconomyResponse bankDeposit(String name, double amount) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported"); }
    @Override
    public EconomyResponse isBankOwner(String name, String playerName) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported"); }
    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported"); }
    @Override
    public EconomyResponse isBankMember(String name, String playerName) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported"); }
    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported"); }
    @Override
    public List<String> getBanks() { return new ArrayList<>(); }
} 