package com.iruanp.mcuniversaleconomy;

import com.iruanp.mcuniversaleconomy.commands.fabric.FabricEconomyCommand;
import com.iruanp.mcuniversaleconomy.config.ModConfig;
import com.iruanp.mcuniversaleconomy.database.DatabaseManager;
import com.iruanp.mcuniversaleconomy.economy.UniversalEconomyService;
import com.iruanp.mcuniversaleconomy.economy.fabric.CommonEconomyProvider;
import com.iruanp.mcuniversaleconomy.lang.LanguageManager;
import com.iruanp.mcuniversaleconomy.notification.fabric.FabricNotificationService;
import com.iruanp.mcuniversaleconomy.notification.fabric.FabricNotificationPlayer;
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
        economyService = new UniversalEconomyService(databaseManager, logger, config, languageManager);

        // Register Common Economy API provider
        CommonEconomyProvider provider = new CommonEconomyProvider(economyService, config);
        CommonEconomy.register("mcuniversaleconomy", provider);

        // Register commands
        FabricEconomyCommand.register(economyService, languageManager, databaseManager, logger);

        // Start notification service when server starts
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // Create notification service
            FabricNotificationService notificationService = new FabricNotificationService(databaseManager, logger, server);

            // Register player join event listener
            ServerPlayConnectionEvents.JOIN.register((handler, sender, joinServer) -> {
                economyService.createAccount(handler.player.getUuid(), handler.player.getName().getString())
                    .thenAccept(success -> {
                        if (success) {
                            notificationService.sendAndRemoveNotifications(new FabricNotificationPlayer(handler.player));
                        } else {
                            LOGGER.error("Failed to create economy account for player: " + handler.player.getName().getString());
                        }
                    });
            });

            // Start notification thread
            Thread notificationThread = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(5000); // Sleep for 5 seconds
                        if (!server.getPlayerManager().getPlayerList().isEmpty()) {
                            server.execute(() -> {
                                notificationService.sendAndRemoveNotificationsToAllOnlinePlayers();
                            });
                        }
                    } catch (InterruptedException e) {
                        LOGGER.error("Notification thread interrupted", e);
                        break;
                    }
                }
            });
            notificationThread.setDaemon(true);
            notificationThread.setName("MCUniversalEconomy-NotificationThread");
            notificationThread.start();
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