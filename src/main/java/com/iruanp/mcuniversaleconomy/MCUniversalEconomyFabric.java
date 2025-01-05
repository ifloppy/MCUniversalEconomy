package com.iruanp.mcuniversaleconomy;

import com.iruanp.mcuniversaleconomy.commands.fabric.FabricEconomyCommand;
import com.iruanp.mcuniversaleconomy.config.ModConfig;
import com.iruanp.mcuniversaleconomy.database.DatabaseManager;
import com.iruanp.mcuniversaleconomy.economy.UniversalEconomyService;
import com.iruanp.mcuniversaleconomy.economy.UniversalEconomyServiceImpl;
import com.iruanp.mcuniversaleconomy.economy.fabric.CommonEconomyProvider;
import com.iruanp.mcuniversaleconomy.lang.LanguageManager;
import com.iruanp.mcuniversaleconomy.util.UnifiedLogger;

import eu.pb4.common.economy.api.CommonEconomy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MCUniversalEconomyFabric implements ModInitializer {
    public static final String MOD_ID = "mcuniversaleconomy";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static UnifiedLogger logger;
    private static ModConfig config;
    private static UniversalEconomyService economyService;
    private static DatabaseManager databaseManager;
    private static LanguageManager languageManager;

    @Override
    public void onInitialize() {
        logger = new UnifiedLogger(LOGGER);

        // Initialize config
        config = new ModConfig();
        config.setLogger(logger);
        config.loadFromYaml(FabricLoader.getInstance().getConfigDir().resolve("mcuniversaleconomy/config.yml"));

        // Initialize database
        databaseManager = new DatabaseManager(config);

        // Initialize language manager
        languageManager = new LanguageManager(FabricLoader.getInstance().getConfigDir().resolve("mcuniversaleconomy").toFile(), config.getLanguage());

        // Initialize economy service
        economyService = new UniversalEconomyServiceImpl(databaseManager, logger, config, languageManager);

        // Register Common Economy API provider
        CommonEconomyProvider provider = new CommonEconomyProvider(economyService, config);
        CommonEconomy.register("mcuniversaleconomy", provider);

        // Register commands
        FabricEconomyCommand.register(economyService, languageManager);

        // Register player join event listener
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            economyService.createAccount(handler.player.getUuid(), handler.player.getName().getString())
                .thenAccept(success -> {
                    if (success) {
                        LOGGER.info("Created economy account for player: " + handler.player.getName().getString());
                    }
                });
        });

        LOGGER.info("MCUniversalEconomy initialized");
    }

    public static ModConfig getConfig() {
        return config;
    }

    public static UniversalEconomyService getEconomyService() {
        return economyService;
    }

    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
} 