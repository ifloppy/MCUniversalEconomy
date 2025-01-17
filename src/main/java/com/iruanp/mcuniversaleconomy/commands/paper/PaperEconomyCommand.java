package com.iruanp.mcuniversaleconomy.commands.paper;

import com.iruanp.mcuniversaleconomy.MCUniversalEconomyPaper;
import com.iruanp.mcuniversaleconomy.commands.EconomyCommand;
import com.iruanp.mcuniversaleconomy.economy.UniversalEconomyService;
import com.iruanp.mcuniversaleconomy.lang.LanguageManager;
import com.iruanp.mcuniversaleconomy.notification.paper.PaperNotificationService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PaperEconomyCommand implements CommandExecutor, TabCompleter {
    private final MCUniversalEconomyPaper plugin;
    private final EconomyCommand economyCommand;
    private final LanguageManager languageManager;
    private final PaperNotificationService notificationService;
    private final UniversalEconomyService economyService;

    public PaperEconomyCommand(MCUniversalEconomyPaper plugin, UniversalEconomyService economyService, LanguageManager languageManager, PaperNotificationService notificationService) {
        this.plugin = plugin;
        this.economyCommand = new EconomyCommand(economyService, languageManager, notificationService);
        this.languageManager = languageManager;
        this.notificationService = notificationService;
        this.economyService = economyService;
    }

    public static void register(MCUniversalEconomyPaper plugin, UniversalEconomyService economyService, LanguageManager languageManager, PaperNotificationService notificationService) {
        PaperEconomyCommand command = new PaperEconomyCommand(plugin, economyService, languageManager, notificationService);
        
        // Register main command and its aliases
        plugin.getCommand("eco").setExecutor(command);
        plugin.getCommand("eco").setTabCompleter(command);
        
        // Register balance command aliases
        plugin.getCommand("balance").setExecutor(command);
        plugin.getCommand("balance").setTabCompleter(command);
        plugin.getCommand("bal").setExecutor(command);
        plugin.getCommand("bal").setTabCompleter(command);
        
        // Register pay command alias
        plugin.getCommand("pay").setExecutor(command);
        plugin.getCommand("pay").setTabCompleter(command);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Handle standalone commands
        switch (label.toLowerCase()) {
            case "balance":
            case "bal":
            case "money":
                handleBalance(sender, args);
                return true;
            case "pay":
                handlePay(sender, args);
                return true;
            case "balancetop":
            case "baltop":
                handleBalanceTop(sender);
                return true;
        }

        // Handle /eco commands
        if (args.length == 0) {
            sender.sendMessage(languageManager.getMessage("usage.eco"));
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "balance":
                handleBalance(sender, args);
                break;
            case "pay":
                handlePay(sender, args);
                break;
            case "give":
                handleGive(sender, args);
                break;
            case "take":
                handleTake(sender, args);
                break;
            case "set":
                handleSet(sender, args);
                break;
            case "balancetop":
            case "baltop":
                handleBalanceTop(sender);
                break;
            case "reload":
                handleReload(sender);
                break;
            default:
                sender.sendMessage(languageManager.getMessage("usage.eco"));
        }
        return true;
    }

    private void handleBalance(CommandSender sender, String[] args) {
        UUID targetUuid;
        String[] adjustedArgs = args;
        
        // If command is used as /eco balance player, we need to skip the first argument
        if (args.length > 0 && args[0].equalsIgnoreCase("balance")) {
            adjustedArgs = new String[args.length - 1];
            System.arraycopy(args, 1, adjustedArgs, 0, args.length - 1);
        }

        if (adjustedArgs.length > 0 && sender.hasPermission("mcuniversaleconomy.admin")) {
            Player target = Bukkit.getPlayer(adjustedArgs[0]);
            if (target == null) {
                sender.sendMessage(languageManager.getMessage("general.player_not_found"));
                return;
            }
            targetUuid = target.getUniqueId();
        } else if (sender instanceof Player) {
            targetUuid = ((Player) sender).getUniqueId();
        } else {
            sender.sendMessage(languageManager.getMessage("balance.console_error"));
            return;
        }

        economyCommand.balance(targetUuid)
            .thenAccept(message -> Bukkit.getScheduler().runTask(plugin, () -> 
                sender.sendMessage(message)));
    }

    private void handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(languageManager.getMessage("general.command_usage"));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(languageManager.getMessage("usage.pay"));
            return;
        }

        Player player = (Player) sender;
        String targetName = args[1];

        try {
            double amount = Double.parseDouble(args[2]);
            economyService.getUuidByUsername(targetName)
                .thenAccept(targetUuid -> {
                    if (targetUuid == null) {
                        Bukkit.getScheduler().runTask(plugin, () ->
                            sender.sendMessage(languageManager.getMessage("general.player_not_found")));
                        return;
                    }

                    if (player.getUniqueId().equals(targetUuid)) {
                        Bukkit.getScheduler().runTask(plugin, () ->
                            sender.sendMessage(languageManager.getMessage("pay.error.self_pay")));
                        return;
                    }

                    economyCommand.pay(player.getUniqueId(), targetUuid, amount)
                        .thenAccept(message -> Bukkit.getScheduler().runTask(plugin, () ->
                            sender.sendMessage(message)));
                });
        } catch (NumberFormatException e) {
            sender.sendMessage(languageManager.getMessage("general.invalid_amount"));
        }
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mcuniversaleconomy.admin")) {
            sender.sendMessage(languageManager.getMessage("general.no_permission"));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(languageManager.getMessage("usage.give"));
            return;
        }

        String targetName = args[1];
        try {
            double amount = Double.parseDouble(args[2]);
            economyService.getUuidByUsername(targetName)
                .thenAccept(targetUuid -> {
                    if (targetUuid == null) {
                        Bukkit.getScheduler().runTask(plugin, () ->
                            sender.sendMessage(languageManager.getMessage("general.player_not_found")));
                        return;
                    }

                    economyCommand.give(targetUuid, amount)
                        .thenAccept(message -> Bukkit.getScheduler().runTask(plugin, () ->
                            sender.sendMessage(message)));
                });
        } catch (NumberFormatException e) {
            sender.sendMessage(languageManager.getMessage("general.invalid_amount"));
        }
    }

    private void handleTake(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mcuniversaleconomy.admin")) {
            sender.sendMessage(languageManager.getMessage("general.no_permission"));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(languageManager.getMessage("usage.take"));
            return;
        }

        String targetName = args[1];
        try {
            double amount = Double.parseDouble(args[2]);
            economyService.getUuidByUsername(targetName)
                .thenAccept(targetUuid -> {
                    if (targetUuid == null) {
                        Bukkit.getScheduler().runTask(plugin, () ->
                            sender.sendMessage(languageManager.getMessage("general.player_not_found")));
                        return;
                    }

                    economyCommand.take(targetUuid, amount)
                        .thenAccept(message -> Bukkit.getScheduler().runTask(plugin, () ->
                            sender.sendMessage(message)));
                });
        } catch (NumberFormatException e) {
            sender.sendMessage(languageManager.getMessage("general.invalid_amount"));
        }
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mcuniversaleconomy.admin")) {
            sender.sendMessage(languageManager.getMessage("general.no_permission"));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(languageManager.getMessage("usage.set"));
            return;
        }

        String targetName = args[1];
        try {
            double amount = Double.parseDouble(args[2]);
            economyService.getUuidByUsername(targetName)
                .thenAccept(targetUuid -> {
                    if (targetUuid == null) {
                        Bukkit.getScheduler().runTask(plugin, () ->
                            sender.sendMessage(languageManager.getMessage("general.player_not_found")));
                        return;
                    }

                    economyCommand.set(targetUuid, amount)
                        .thenAccept(message -> Bukkit.getScheduler().runTask(plugin, () ->
                            sender.sendMessage(message)));
                });
        } catch (NumberFormatException e) {
            sender.sendMessage(languageManager.getMessage("general.invalid_amount"));
        }
    }

    private void handleBalanceTop(CommandSender sender) {
        if (!sender.hasPermission("mcuniversaleconomy.admin")) {
            sender.sendMessage(languageManager.getMessage("general.no_permission"));
            return;
        }
        economyCommand.balanceTop(10)
            .thenAccept(message -> Bukkit.getScheduler().runTask(plugin, () -> 
                sender.sendMessage(message.split("\n"))));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("mcuniversaleconomy.admin")) {
            sender.sendMessage(languageManager.getMessage("general.no_permission"));
            return;
        }
        economyCommand.reload()
            .thenAccept(message -> Bukkit.getScheduler().runTask(plugin, () -> 
                sender.sendMessage(message)));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("balance");
            completions.add("balancetop");
            completions.add("pay");
            if (sender.hasPermission("mcuniversaleconomy.admin")) {
                completions.add("give");
                completions.add("take");
                completions.add("set");
                completions.add("reload");
            }
        } else if (args.length == 2) {
            // For all player-related commands, suggest all known player names from the database
            if (args[0].equalsIgnoreCase("pay") || 
                (sender.hasPermission("mcuniversaleconomy.admin") && 
                (args[0].equalsIgnoreCase("give") || 
                 args[0].equalsIgnoreCase("take") || 
                 args[0].equalsIgnoreCase("set") ||
                 args[0].equalsIgnoreCase("balance")))) {
                economyService.getAllPlayerNames().join().forEach(completions::add);
            }
        }

        return completions;
    }
} 