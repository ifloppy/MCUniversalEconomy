package com.iruanp.mcuniversaleconomy.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iruanp.mcuniversaleconomy.util.UnifiedLogger;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    // Database settings
    private String databaseHost = "localhost";
    private int databasePort = 3306;
    private String databaseName = "mceconomy";
    private String databaseUsername = "your_username";
    private String databasePassword = "your_password";
    private String tablePrefix = "mcue_";

    // General settings
    private double initialBalance = 0.0;
    private String currencySymbol = "$";
    private String currencyFormat = "#,##0.00";
    private String language = "en_US";
    private boolean checkUpdates = true;
    private String serverId = null;  // Server identifier, null by default

    // Transaction settings
    private boolean enableLogging = true;
    private double paymentTax = 0.0;
    private double minimumPayment = 0.0;
    private double maximumPayment = -1.0;

    private UnifiedLogger logger;
    private Path configPath;

    // Load config for both platforms
    public void loadFromYaml(Path path) {
        this.configPath = path;
        File file = path.toFile();
        if (!file.exists()) {
            // Save default config
            saveDefaultConfig(file);
        }
        
        YamlHandler yamlHandler = new YamlHandler();
        try {
            yamlHandler.load(path);

            // Load database settings
            databaseHost = yamlHandler.getString("database.host", databaseHost);
            databasePort = yamlHandler.getInt("database.port", databasePort);
            databaseName = yamlHandler.getString("database.name", databaseName);
            databaseUsername = yamlHandler.getString("database.username", databaseUsername);
            databasePassword = yamlHandler.getString("database.password", databasePassword);
            tablePrefix = yamlHandler.getString("database.table_prefix", tablePrefix);

            // Load general settings
            initialBalance = yamlHandler.getDouble("settings.initial_balance", initialBalance);
            currencySymbol = yamlHandler.getString("settings.currency_symbol", currencySymbol);
            currencyFormat = yamlHandler.getString("settings.currency_format", currencyFormat);
            language = yamlHandler.getString("settings.language", language);
            checkUpdates = yamlHandler.getBoolean("settings.check_updates", checkUpdates);
            serverId = yamlHandler.getString("settings.server_id", serverId);

            // Load transaction settings
            enableLogging = yamlHandler.getBoolean("transactions.enable_logging", enableLogging);
            paymentTax = yamlHandler.getDouble("transactions.payment_tax", paymentTax);
            minimumPayment = yamlHandler.getDouble("transactions.minimum_payment", minimumPayment);
            maximumPayment = yamlHandler.getDouble("transactions.maximum_payment", maximumPayment);
        } catch (IOException e) {
            logger.error("Could not load config: " + e.getMessage());
        }
    }

    private void saveDefaultConfig(File file) {
        try {
            file.getParentFile().mkdirs();
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                if (in != null) {
                    Files.copy(in, file.toPath());
                }
            }
        } catch (IOException e) {
            logger.error("Could not save default config: " + e.getMessage());
        }
    }

    // Load config for Fabric platform (JSON format)
    public void loadFromJson(Path configPath) {
        try {
            if (!Files.exists(configPath)) {
                Files.createDirectories(configPath.getParent());
                saveToJson(configPath);
                return;
            }

            try (Reader reader = Files.newBufferedReader(configPath)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                ModConfig loaded = gson.fromJson(reader, ModConfig.class);
                // Copy all properties from loaded config
                this.databaseHost = loaded.databaseHost;
                this.databasePort = loaded.databasePort;
                this.databaseName = loaded.databaseName;
                this.databaseUsername = loaded.databaseUsername;
                this.databasePassword = loaded.databasePassword;
                this.tablePrefix = loaded.tablePrefix;
                this.initialBalance = loaded.initialBalance;
                this.currencySymbol = loaded.currencySymbol;
                this.currencyFormat = loaded.currencyFormat;
                this.language = loaded.language;
                this.checkUpdates = loaded.checkUpdates;
                this.serverId = loaded.serverId;
                this.enableLogging = loaded.enableLogging;
                this.paymentTax = loaded.paymentTax;
                this.minimumPayment = loaded.minimumPayment;
                this.maximumPayment = loaded.maximumPayment;
            }
        } catch (IOException e) {
            logger.severe("Failed to load config: " + e.getMessage());
        }
    }

    public void saveToJson(Path configPath) {
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(this, writer);
            }
        } catch (IOException e) {
            logger.severe("Failed to save config: " + e.getMessage());
        }
    }

    // Getters and setters
    public String getDatabaseHost() { return databaseHost; }
    public void setDatabaseHost(String databaseHost) { this.databaseHost = databaseHost; }
    
    public int getDatabasePort() { return databasePort; }
    public void setDatabasePort(int databasePort) { this.databasePort = databasePort; }
    
    public String getDatabaseName() { return databaseName; }
    public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }
    
    public String getDatabaseUsername() { return databaseUsername; }
    public void setDatabaseUsername(String databaseUsername) { this.databaseUsername = databaseUsername; }
    
    public String getDatabasePassword() { return databasePassword; }
    public void setDatabasePassword(String databasePassword) { this.databasePassword = databasePassword; }
    
    public String getTablePrefix() { return tablePrefix; }
    public void setTablePrefix(String tablePrefix) { this.tablePrefix = tablePrefix; }

    public double getInitialBalance() { return initialBalance; }
    public void setInitialBalance(double initialBalance) { this.initialBalance = initialBalance; }

    public String getCurrencySymbol() { return currencySymbol; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }

    public String getCurrencyFormat() { return currencyFormat; }
    public void setCurrencyFormat(String currencyFormat) { this.currencyFormat = currencyFormat; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public boolean isCheckUpdates() { return checkUpdates; }
    public void setCheckUpdates(boolean checkUpdates) { this.checkUpdates = checkUpdates; }

    public boolean isEnableLogging() { return enableLogging; }
    public void setEnableLogging(boolean enableLogging) { this.enableLogging = enableLogging; }

    public double getPaymentTax() { return paymentTax; }
    public void setPaymentTax(double paymentTax) { this.paymentTax = paymentTax; }

    public double getMinimumPayment() { return minimumPayment; }
    public void setMinimumPayment(double minimumPayment) { this.minimumPayment = minimumPayment; }

    public double getMaximumPayment() { return maximumPayment; }
    public void setMaximumPayment(double maximumPayment) { this.maximumPayment = maximumPayment; }

    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }

    public UnifiedLogger getLogger() { return logger; }
    public void setLogger(UnifiedLogger logger) { this.logger = logger; }

    public Path getConfigPath() { return configPath; }
}