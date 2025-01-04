package com.iruanp.mcuniversaleconomy;

import com.iruanp.mcuniversaleconomy.commands.fabric.FabricEconomyCommand;
import com.iruanp.mcuniversaleconomy.config.ModConfig;
import com.iruanp.mcuniversaleconomy.database.DatabaseManager;
import com.iruanp.mcuniversaleconomy.economy.UniversalEconomyService;
import com.iruanp.mcuniversaleconomy.economy.UniversalEconomyServiceImpl;
import com.iruanp.mcuniversaleconomy.economy.fabric.CommonEconomyProvider;
import com.iruanp.mcuniversaleconomy.util.UnifiedLogger;

import eu.pb4.common.economy.api.CommonEconomy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class MCUniversalEconomyFabric implements ModInitializer {
    public static final String MOD_ID = "mcuniversaleconomy";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static ModConfig config;
    private static UniversalEconomyService economyService;
    private static DatabaseManager databaseManager;

    @Override
    public void onInitialize() {
        // Initialize config
        config = new ModConfig();
        config.setLogger(new UnifiedLogger(LOGGER));
        config.loadFromYaml(FabricLoader.getInstance().getConfigDir().resolve("mcuniversaleconomy/config.yml"));

        // Initialize database
        databaseManager = new DatabaseManager(config);

        // Initialize economy service
        economyService = new UniversalEconomyServiceImpl(databaseManager, new UnifiedLogger(LOGGER), config);

        // Register commands
        FabricEconomyCommand.register(economyService);

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

    private ModConfig loadConfig() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
        Path configPath = configDir.resolve("config.json");
        return null;
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