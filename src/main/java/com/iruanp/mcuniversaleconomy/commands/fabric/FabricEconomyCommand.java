package com.iruanp.mcuniversaleconomy.commands.fabric;

import com.iruanp.mcuniversaleconomy.commands.EconomyCommand;
import com.iruanp.mcuniversaleconomy.economy.UniversalEconomyService;
import com.iruanp.mcuniversaleconomy.lang.LanguageManager;
import com.iruanp.mcuniversaleconomy.notification.fabric.FabricNotificationPlayer;
import com.iruanp.mcuniversaleconomy.notification.fabric.FabricNotificationService;
import com.iruanp.mcuniversaleconomy.database.DatabaseManager;
import com.iruanp.mcuniversaleconomy.util.UnifiedLogger;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.server.MinecraftServer;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FabricEconomyCommand {
    private final EconomyCommand economyCommand;
    private final LanguageManager languageManager;
    private final UniversalEconomyService economyService;
    private FabricNotificationService notificationService;
    private final DatabaseManager databaseManager;
    private final UnifiedLogger logger;

    public FabricEconomyCommand(UniversalEconomyService economyService, LanguageManager languageManager, DatabaseManager databaseManager, UnifiedLogger logger) {
        this.databaseManager = databaseManager;
        this.logger = logger;
        this.economyCommand = new EconomyCommand(economyService, languageManager, null);
        this.languageManager = languageManager;
        this.economyService = economyService;
    }

    public static void register(UniversalEconomyService economyService, LanguageManager languageManager, DatabaseManager databaseManager, UnifiedLogger logger) {
        FabricEconomyCommand command = new FabricEconomyCommand(economyService, languageManager, databaseManager, logger);
        
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // Main /eco command
            final var ecoCommand = literal("eco")
                .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.use", true))
                .executes(command::showUsage)
                .then(literal("balance")
                    .executes(ctx -> command.handleBalance(ctx, null))
                    .then(argument("player", GameProfileArgumentType.gameProfile())
                        .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.admin", false))
                        .executes(ctx -> command.handleBalance(ctx, GameProfileArgumentType.getProfileArgument(ctx, "player")))
                    )
                )
                .then(literal("balancetop")
                    .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.admin", false))
                    .executes(command::handleBalanceTop)
                )
                .then(literal("pay")
                    .then(argument("player", GameProfileArgumentType.gameProfile())
                        .then(argument("amount", DoubleArgumentType.doubleArg(0.0))
                            .executes(command::handlePay)
                        )
                    )
                )
                .then(literal("give")
                    .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.admin", false))
                    .then(argument("player", GameProfileArgumentType.gameProfile())
                        .then(argument("amount", DoubleArgumentType.doubleArg(0.0))
                            .executes(command::handleGive)
                        )
                    )
                )
                .then(literal("take")
                    .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.admin", false))
                    .then(argument("player", GameProfileArgumentType.gameProfile())
                        .then(argument("amount", DoubleArgumentType.doubleArg(0.0))
                            .executes(command::handleTake)
                        )
                    )
                )
                .then(literal("set")
                    .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.admin", false))
                    .then(argument("player", GameProfileArgumentType.gameProfile())
                        .then(argument("amount", DoubleArgumentType.doubleArg(0.0))
                            .executes(command::handleSet)
                        )
                    )
                )
                .then(literal("reload")
                    .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.admin", false))
                    .executes(command::handleReload)
                );

            // Register command and aliases
            dispatcher.register(ecoCommand);
            
            // Register economy and money as direct aliases
            final var economyCommand = literal("economy")
                .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.use", true))
                .executes(command::showUsage)
                .then(literal("balance")
                    .executes(ctx -> command.handleBalance(ctx, null))
                    .then(argument("player", GameProfileArgumentType.gameProfile())
                        .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.admin", false))
                        .executes(ctx -> command.handleBalance(ctx, GameProfileArgumentType.getProfileArgument(ctx, "player")))
                    )
                )
                .then(literal("balancetop")
                    .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.admin", false))
                    .executes(command::handleBalanceTop)
                )
                .then(literal("pay")
                    .then(argument("player", GameProfileArgumentType.gameProfile())
                        .then(argument("amount", DoubleArgumentType.doubleArg(0.0))
                            .executes(command::handlePay)
                        )
                    )
                )
                .then(literal("give")
                    .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.admin", false))
                    .then(argument("player", GameProfileArgumentType.gameProfile())
                        .then(argument("amount", DoubleArgumentType.doubleArg(0.0))
                            .executes(command::handleGive)
                        )
                    )
                )
                .then(literal("take")
                    .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.admin", false))
                    .then(argument("player", GameProfileArgumentType.gameProfile())
                        .then(argument("amount", DoubleArgumentType.doubleArg(0.0))
                            .executes(command::handleTake)
                        )
                    )
                )
                .then(literal("set")
                    .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.admin", false))
                    .then(argument("player", GameProfileArgumentType.gameProfile())
                        .then(argument("amount", DoubleArgumentType.doubleArg(0.0))
                            .executes(command::handleSet)
                        )
                    )
                );
            dispatcher.register(economyCommand);

            final var moneyCommand = literal("money")
                .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.use", true))
                .executes(command::showUsage)
                .then(literal("balance")
                    .executes(ctx -> command.handleBalance(ctx, null))
                    .then(argument("player", GameProfileArgumentType.gameProfile())
                        .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.admin", false))
                        .executes(ctx -> command.handleBalance(ctx, GameProfileArgumentType.getProfileArgument(ctx, "player")))
                    )
                )
                .then(literal("balancetop")
                    .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.admin", false))
                    .executes(command::handleBalanceTop)
                )
                .then(literal("pay")
                    .then(argument("player", GameProfileArgumentType.gameProfile())
                        .then(argument("amount", DoubleArgumentType.doubleArg(0.0))
                            .executes(command::handlePay)
                        )
                    )
                )
                .then(literal("give")
                    .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.admin", false))
                    .then(argument("player", GameProfileArgumentType.gameProfile())
                        .then(argument("amount", DoubleArgumentType.doubleArg(0.0))
                            .executes(command::handleGive)
                        )
                    )
                )
                .then(literal("take")
                    .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.admin", false))
                    .then(argument("player", GameProfileArgumentType.gameProfile())
                        .then(argument("amount", DoubleArgumentType.doubleArg(0.0))
                            .executes(command::handleTake)
                        )
                    )
                )
                .then(literal("set")
                    .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.admin", false))
                    .then(argument("player", GameProfileArgumentType.gameProfile())
                        .then(argument("amount", DoubleArgumentType.doubleArg(0.0))
                            .executes(command::handleSet)
                        )
                    )
                );
            dispatcher.register(moneyCommand);

            // Register balance command aliases
            final var balanceCommand = literal("balance")
                .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.use", true))
                .executes(ctx -> command.handleBalance(ctx, null))
                .then(argument("player", GameProfileArgumentType.gameProfile())
                .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.admin", false))
                    .executes(ctx -> command.handleBalance(ctx, GameProfileArgumentType.getProfileArgument(ctx, "player")))
                );
            dispatcher.register(balanceCommand);
            
            // Register 'bal' as a separate command with the same implementation
            final var balAlias = literal("bal")
                .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.use", true))
                .executes(ctx -> command.handleBalance(ctx, null))
                .then(argument("player", GameProfileArgumentType.gameProfile())
                .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.admin", false))
                    .executes(ctx -> command.handleBalance(ctx, GameProfileArgumentType.getProfileArgument(ctx, "player")))
                );
            dispatcher.register(balAlias);

            // Register pay command alias
            final var payCommand = literal("pay")
                .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.use", true))
                .then(argument("player", GameProfileArgumentType.gameProfile())
                    .then(argument("amount", DoubleArgumentType.doubleArg(0.0))
                        .executes(command::handlePay)
                    )
                );
            dispatcher.register(payCommand);

            // Register balancetop command alias
            final var balanceTopCommand = literal("balancetop")
                .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.admin", false))
                .executes(command::handleBalanceTop);
            dispatcher.register(balanceTopCommand);
            
            // Register 'baltop' as a separate command with the same implementation
            final var balTopAlias = literal("baltop")
                .requires(source -> source.hasPermissionLevel(4) || Permissions.check(source, "mcuniversaleconomy.admin", false))
                .executes(command::handleBalanceTop);
            dispatcher.register(balTopAlias);
        });

        // Register notification service when server starts
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            command.initializeNotificationService(server);
        });
    }

    private void initializeNotificationService(MinecraftServer server) {
        this.notificationService = new FabricNotificationService(databaseManager, logger, server);
        this.economyCommand.setNotificationService(notificationService);

        // Register player join event listener
        ServerPlayConnectionEvents.JOIN.register((handler, sender, joinServer) -> {
            economyService.createAccount(handler.player.getUuid(), handler.player.getName().getString())
                .thenAccept(success -> {
                    if (success) {
                        notificationService.sendAndRemoveNotifications(new FabricNotificationPlayer(handler.player));
                    } else {
                        logger.error("Failed to create economy account for player: " + handler.player.getName().getString());
                    }
                });
        });

        // Start notification thread
        Thread notificationThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000); // Sleep for 5 seconds
                    if (!server.getPlayerManager().getPlayerList().isEmpty()) {
                        notificationService.sendAndRemoveNotificationsToAllOnlinePlayers();
                    }
                } catch (InterruptedException e) {
                    logger.error("Notification thread interrupted", e);
                    break;
                }
            }
        });
        notificationThread.setDaemon(true);
        notificationThread.setName("MCUniversalEconomy-NotificationThread");
        notificationThread.start();
    }

    private int showUsage(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendMessage(Text.literal(languageManager.getMessage("usage.eco")));
        return Command.SINGLE_SUCCESS;
    }

    private int handleBalance(CommandContext<ServerCommandSource> ctx, Collection<com.mojang.authlib.GameProfile> targets) {
        ServerCommandSource source = ctx.getSource();

        if (targets == null || targets.isEmpty()) {
            try {
                ServerPlayerEntity player = source.getPlayerOrThrow();
                economyCommand.balance(player.getUuid())
                    .thenAccept(message -> source.sendMessage(Text.literal(message)));
            } catch (Exception e) {
                source.sendMessage(Text.literal(languageManager.getMessage("balance.console_error")));
                return 0;
            }
        } else {
            com.mojang.authlib.GameProfile target = targets.iterator().next();
            economyCommand.balance(target.getId())
                .thenAccept(message -> source.sendMessage(Text.literal(message)));
        }

        return Command.SINGLE_SUCCESS;
    }

    private int handlePay(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(ctx, "player");
            if (profiles.isEmpty()) {
                source.sendMessage(Text.literal(languageManager.getMessage("general.player_not_found")));
                return 0;
            }
            GameProfile targetProfile = profiles.iterator().next();
            UUID targetUuid = targetProfile.getId();

            if (player.getUuid().equals(targetUuid)) {
                source.sendMessage(Text.literal(languageManager.getMessage("pay.error.self_pay")));
                return 0;
            }

            economyCommand.pay(player.getUuid(), targetUuid, DoubleArgumentType.getDouble(ctx, "amount"))
                .thenAccept(message -> source.sendMessage(Text.literal(message)));

            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendMessage(Text.literal(languageManager.getMessage("general.command_error")));
            logger.error("Error executing pay command", e);
            return 0;
        }
    }

    private int handleGive(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        try {
            Collection<com.mojang.authlib.GameProfile> targets = GameProfileArgumentType.getProfileArgument(ctx, "player");
            if (targets.isEmpty()) {
                source.sendMessage(Text.literal(languageManager.getMessage("general.player_not_found")));
                return 0;
            }
            com.mojang.authlib.GameProfile target = targets.iterator().next();
            double amount = DoubleArgumentType.getDouble(ctx, "amount");

            economyCommand.give(target.getId(), amount)
                .thenAccept(message -> source.sendMessage(Text.literal(message)));

            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            source.sendMessage(Text.literal(languageManager.getMessage("general.player_not_found")));
            return 0;
        }
    }

    private int handleTake(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        try {
            Collection<com.mojang.authlib.GameProfile> targets = GameProfileArgumentType.getProfileArgument(ctx, "player");
            if (targets.isEmpty()) {
                source.sendMessage(Text.literal(languageManager.getMessage("general.player_not_found")));
                return 0;
            }
            com.mojang.authlib.GameProfile target = targets.iterator().next();
            double amount = DoubleArgumentType.getDouble(ctx, "amount");

            economyCommand.take(target.getId(), amount)
                .thenAccept(message -> source.sendMessage(Text.literal(message)));

            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            source.sendMessage(Text.literal(languageManager.getMessage("general.player_not_found")));
            return 0;
        }
    }

    private int handleSet(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        try {
            Collection<com.mojang.authlib.GameProfile> targets = GameProfileArgumentType.getProfileArgument(ctx, "player");
            if (targets.isEmpty()) {
                source.sendMessage(Text.literal(languageManager.getMessage("general.player_not_found")));
                return 0;
            }
            com.mojang.authlib.GameProfile target = targets.iterator().next();
            double amount = DoubleArgumentType.getDouble(ctx, "amount");

            economyCommand.set(target.getId(), amount)
                .thenAccept(message -> source.sendMessage(Text.literal(message)));

            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            source.sendMessage(Text.literal(languageManager.getMessage("general.player_not_found")));
            return 0;
        }
    }

    private int handleBalanceTop(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        economyCommand.balanceTop(10)
            .thenAccept(message -> source.sendMessage(Text.literal(message)));
        return Command.SINGLE_SUCCESS;
    }

    private int handleReload(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        economyCommand.reload()
            .thenAccept(message -> source.sendMessage(Text.literal(message)));
        return Command.SINGLE_SUCCESS;
    }
} 