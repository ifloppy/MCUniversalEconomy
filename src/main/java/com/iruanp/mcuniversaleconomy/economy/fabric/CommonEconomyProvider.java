package com.iruanp.mcuniversaleconomy.economy.fabric;

import com.iruanp.mcuniversaleconomy.economy.UniversalEconomyService;
import com.mojang.authlib.GameProfile;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class CommonEconomyProvider implements EconomyProvider {
    private final UniversalEconomyService economyService;
    private final EconomyCurrency currency;

    public CommonEconomyProvider(UniversalEconomyService economyService) {
        this.economyService = economyService;
        this.currency = new CommonEconomyCurrency(this);
    }

    @Override
    public Text name() {
        return Text.literal("MCUniversalEconomy");
    }

    @Override
    @Nullable
    public EconomyAccount getAccount(MinecraftServer server, GameProfile profile, String account) {
        return new CommonEconomyAccount(economyService, profile.getId(), profile.getName(), this.currency, this, 
            new net.minecraft.util.Identifier("mcuniversaleconomy", account));
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