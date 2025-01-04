package com.iruanp.mcuniversaleconomy;

import com.iruanp.mcuniversaleconomy.commands.paper.PaperEconomyCommand;
import com.iruanp.mcuniversaleconomy.config.ModConfig;
import com.iruanp.mcuniversaleconomy.database.DatabaseManager;
import com.iruanp.mcuniversaleconomy.economy.UniversalEconomyService;
import com.iruanp.mcuniversaleconomy.economy.UniversalEconomyServiceImpl;
import com.iruanp.mcuniversaleconomy.economy.paper.VaultEconomyProvider;
import com.iruanp.mcuniversaleconomy.lang.LanguageManager;
import com.iruanp.mcuniversaleconomy.util.UnifiedLogger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class MCUniversalEconomyPaper extends JavaPlugin implements Listener {
    private static ModConfig config;
    private static UniversalEconomyService economyService;
    private static DatabaseManager databaseManager;
    private static LanguageManager languageManager;
    private static UnifiedLogger logger;

    @Override
    public void onEnable() {
        // Initialize config
        config = new ModConfig();
        logger = new UnifiedLogger(getLogger());
        config.setLogger(logger);
        config.loadFromYaml(getDataFolder().toPath().resolve("config.yml"));

        // Initialize database
        databaseManager = new DatabaseManager(config);

        // Initialize language manager
        languageManager = new LanguageManager(getDataFolder(), config.getLanguage());

        // Initialize economy service
        economyService = new UniversalEconomyServiceImpl(databaseManager, logger, config, languageManager);

        // Register commands
        PaperEconomyCommand.register(this, economyService, languageManager);

        // Register event listeners
        getServer().getPluginManager().registerEvents(this, this);

        // Register Vault economy provider if Vault is present
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            VaultEconomyProvider vaultEconomyProvider = new VaultEconomyProvider(economyService, this);
            getServer().getServicesManager().register(Economy.class, vaultEconomyProvider, this, ServicePriority.Normal);
            getLogger().info("Vault integration enabled");
        }

        getLogger().info("MCUniversalEconomy enabled");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        economyService.createAccount(event.getPlayer().getUniqueId(), event.getPlayer().getName())
            .thenAccept(success -> {
                if (success) {
                    getLogger().info("Created economy account for player: " + event.getPlayer().getName());
                }
            });
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("MCUniversalEconomy disabled");
    }

    public static ModConfig getModConfig() {
        return config;
    }

    public static UniversalEconomyService getEconomyService() {
        return economyService;
    }

    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
} 