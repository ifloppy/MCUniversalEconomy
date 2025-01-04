package com.iruanp.mcuniversaleconomy.economy.fabric;

import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CommonEconomyCurrency implements EconomyCurrency {
    private final EconomyProvider provider;

    public CommonEconomyCurrency(EconomyProvider provider) {
        this.provider = provider;
    }

    @Override
    public Text name() {
        return Text.literal("Coin");
    }

    @Override
    public Identifier id() {
        return new Identifier(provider.id(), "coin");
    }

    @Override
    public String formatValue(long value, boolean precise) {
        if (precise) {
            return String.valueOf(value);
        }
        return value + " Coin" + (value == 1 ? "" : "s");
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