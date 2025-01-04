package com.iruanp.mcuniversaleconomy.commands.paper;

import com.iruanp.mcuniversaleconomy.MCUniversalEconomyPaper;
import com.iruanp.mcuniversaleconomy.commands.EconomyCommand;
import com.iruanp.mcuniversaleconomy.economy.UniversalEconomyService;
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

    public PaperEconomyCommand(MCUniversalEconomyPaper plugin, UniversalEconomyService economyService) {
        this.plugin = plugin;
        this.economyCommand = new EconomyCommand(economyService);
    }

    public static void register(MCUniversalEconomyPaper plugin, UniversalEconomyService economyService) {
        PaperEconomyCommand command = new PaperEconomyCommand(plugin, economyService);
        
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
        }

        // Handle /eco commands
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /eco <balance|pay|give|take|set>");
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
            default:
                sender.sendMessage("§cUnknown command. Use /eco <balance|pay|give|take|set>");
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
                sender.sendMessage("§cPlayer not found");
                return;
            }
            targetUuid = target.getUniqueId();
        } else if (sender instanceof Player) {
            targetUuid = ((Player) sender).getUniqueId();
        } else {
            sender.sendMessage("§cOnly players can check their balance");
            return;
        }

        economyCommand.balance(targetUuid)
            .thenAccept(message -> Bukkit.getScheduler().runTask(plugin, () -> 
                sender.sendMessage(message)));
    }

    private void handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command");
            return;
        }

        String[] adjustedArgs = args;
        // If command is used as /eco pay player amount, we need to skip the first argument
        if (args.length > 0 && args[0].equalsIgnoreCase("pay")) {
            adjustedArgs = new String[args.length - 1];
            System.arraycopy(args, 1, adjustedArgs, 0, args.length - 1);
        }

        if (adjustedArgs.length < 2) {
            sender.sendMessage("§cUsage: /pay <player> <amount>");
            return;
        }

        Player target = Bukkit.getPlayer(adjustedArgs[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found");
            return;
        }

        try {
            double amount = Double.parseDouble(adjustedArgs[1]);
            economyCommand.pay(((Player) sender).getUniqueId(), target.getUniqueId(), amount)
                .thenAccept(message -> Bukkit.getScheduler().runTask(plugin, () -> 
                    sender.sendMessage(message)));
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount");
        }
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mcuniversaleconomy.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command");
            return;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /eco give <player> <amount>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found");
            return;
        }

        try {
            double amount = Double.parseDouble(args[2]);
            economyCommand.give(target.getUniqueId(), amount)
                .thenAccept(message -> Bukkit.getScheduler().runTask(plugin, () -> 
                    sender.sendMessage(message)));
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount");
        }
    }

    private void handleTake(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mcuniversaleconomy.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command");
            return;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /eco take <player> <amount>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found");
            return;
        }

        try {
            double amount = Double.parseDouble(args[2]);
            economyCommand.take(target.getUniqueId(), amount)
                .thenAccept(message -> Bukkit.getScheduler().runTask(plugin, () -> 
                    sender.sendMessage(message)));
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount");
        }
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mcuniversaleconomy.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command");
            return;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /eco set <player> <amount>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found");
            return;
        }

        try {
            double amount = Double.parseDouble(args[2]);
            economyCommand.set(target.getUniqueId(), amount)
                .thenAccept(message -> Bukkit.getScheduler().runTask(plugin, () -> 
                    sender.sendMessage(message)));
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("balance");
            completions.add("pay");
            if (sender.hasPermission("mcuniversaleconomy.admin")) {
                completions.add("give");
                completions.add("take");
                completions.add("set");
            }
        } else if (args.length == 2) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        }

        return completions;
    }
} 