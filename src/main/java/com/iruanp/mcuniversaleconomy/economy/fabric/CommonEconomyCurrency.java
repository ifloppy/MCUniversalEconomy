package com.iruanp.mcuniversaleconomy.economy.fabric;

import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import com.iruanp.mcuniversaleconomy.config.ModConfig;

public class CommonEconomyCurrency implements EconomyCurrency {
    private final EconomyProvider provider;
    private final ModConfig config;

    public CommonEconomyCurrency(EconomyProvider provider, ModConfig config) {
        this.provider = provider;
        this.config = config;
    }

    @Override
    public Text name() {
        return Text.literal("default_currency");
    }

    @Override
    public Identifier id() {
        return new Identifier(provider.id(), "default_currency");
    }

    @Override
    public String formatValue(long value, boolean precise) {
        if (precise) {
            return String.valueOf(value);
        }
        return config.getCurrencySymbol() + value;
    }

    @Override
    public long parseValue(String value) throws NumberFormatException {
        return Long.parseLong(value.replaceAll("[^0-9]", ""));
    }

    @Override
    public EconomyProvider provider() {
        return provider;
    }

    @Override
    public ItemStack icon() {
        return Items.GOLD_INGOT.getDefaultStack();
    }
} 