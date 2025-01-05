package com.iruanp.mcuniversaleconomy.economy.fabric;

import com.iruanp.mcuniversaleconomy.economy.UniversalEconomyService;
import com.iruanp.mcuniversaleconomy.config.ModConfig;
import com.mojang.authlib.GameProfile;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class CommonEconomyProvider implements EconomyProvider {
    private final UniversalEconomyService economyService;
    private final EconomyCurrency currency;
    private final ModConfig config;

    public CommonEconomyProvider(UniversalEconomyService economyService, ModConfig config) {
        this.economyService = economyService;
        this.config = config;
        this.currency = new CommonEconomyCurrency(this, config);
    }

    @Override
    public Text name() {
        return Text.literal("MCUniversalEconomy");
    }

    @Override
    @Nullable
    public EconomyAccount getAccount(MinecraftServer server, GameProfile profile, String account) {
        return new CommonEconomyAccount(economyService, profile.getId(), profile.getName(), this.currency, this, 
            Identifier.of("mcuniversaleconomy", account));
    }

    @Override
    @Nullable
    public String defaultAccount(MinecraftServer server, GameProfile profile, EconomyCurrency currency) {
        return "default";
    }

    @Override
    public Collection<EconomyAccount> getAccounts(MinecraftServer server, GameProfile profile) {
        return Collections.singletonList(getAccount(server, profile, "default"));
    }

    @Override
    public Collection<EconomyCurrency> getCurrencies(MinecraftServer server) {
        return Collections.singletonList(currency);
    }

    @Override
    public ItemStack icon() {
        return Items.GOLD_INGOT.getDefaultStack();
    }

    @Override
    @Nullable
    public EconomyCurrency getCurrency(MinecraftServer server, String id) {
        return currency;
    }
} 