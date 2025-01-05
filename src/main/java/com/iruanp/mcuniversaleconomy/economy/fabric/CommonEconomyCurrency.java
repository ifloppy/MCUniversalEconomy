package com.iruanp.mcuniversaleconomy.economy.fabric;

import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import com.iruanp.mcuniversaleconomy.config.ModConfig;
import java.text.DecimalFormat;
import java.text.ParseException;

public class CommonEconomyCurrency implements EconomyCurrency {
    private final EconomyProvider provider;
    private final ModConfig config;
    private final DecimalFormat decimalFormat;
    private final int decimalPlaces;
    private final long decimalMultiplier;

    public CommonEconomyCurrency(EconomyProvider provider, ModConfig config) {
        this.provider = provider;
        this.config = config;
        this.decimalFormat = new DecimalFormat(config.getCurrencyFormat());
        // Calculate decimal places from format string
        String format = config.getCurrencyFormat();
        this.decimalPlaces = format.length() - format.indexOf('.') - 1;
        this.decimalMultiplier = (long) Math.pow(10, decimalPlaces);
    }

    public long getDecimalMultiplier() {
        return decimalMultiplier;
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
        double realValue = (double) value / decimalMultiplier;
        if (precise) {
            return decimalFormat.format(realValue);
        }
        return config.getCurrencySymbol() + decimalFormat.format(realValue);
    }

    @Override
    public long parseValue(String value) throws NumberFormatException {
        try {
            // Remove currency symbol and any non-numeric characters except decimal point and minus
            String cleanValue = value.replace(config.getCurrencySymbol(), "").replaceAll("[^0-9.-]", "");
            double parsedValue = decimalFormat.parse(cleanValue).doubleValue();
            return Math.round(parsedValue * decimalMultiplier);
        } catch (ParseException e) {
            throw new NumberFormatException("Invalid currency format: " + value);
        }
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