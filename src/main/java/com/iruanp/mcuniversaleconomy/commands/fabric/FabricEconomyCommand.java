package com.iruanp.mcuniversaleconomy.commands.fabric;

import com.iruanp.mcuniversaleconomy.commands.EconomyCommand;
import com.iruanp.mcuniversaleconomy.economy.UniversalEconomyService;
import com.iruanp.mcuniversaleconomy.lang.LanguageManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FabricEconomyCommand {
    private final EconomyCommand economyCommand;
    private final LanguageManager languageManager;

    public FabricEconomyCommand(UniversalEconomyService economyService, LanguageManager languageManager) {
        this.economyCommand = new EconomyCommand(economyService, languageManager);
        this.languageManager = languageManager;
    }

    public static void register(UniversalEconomyService economyService, LanguageManager languageManager) {
        FabricEconomyCommand command = new FabricEconomyCommand(economyService, languageManager);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // Main /eco command
            var ecoCommand = literal("eco")
                .requires(source -> Permissions.check(source, "mcuniversaleconomy.use", true))
                .executes(command::showUsage)
                .then(literal("balance")
                    .executes(ctx -> command.handleBalance(ctx, null))
                    .then(argument("player", EntityArgumentType.player())
                        .requires(source -> Permissions.check(source, "mcuniversaleconomy.admin", false))
                        .executes(ctx -> command.handleBalance(ctx, EntityArgumentType.getPlayer(ctx, "player")))
                    )
                )
                .then(literal("pay")
                    .then(argument("player", EntityArgumentType.player())
                        .then(argument("amount", DoubleArgumentType.doubleArg(0.0))
                            .executes(command::handlePay)
                        )
                    )
                )
                .then(literal("give")
                    .requires(source -> Permissions.check(source, "mcuniversaleconomy.admin", false))
                    .then(argument("player", EntityArgumentType.player())
                        .then(argument("amount", DoubleArgumentType.doubleArg(0.0))
                            .executes(command::handleGive)
                        )
                    )
                )
                .then(literal("take")
                    .requires(source -> Permissions.check(source, "mcuniversaleconomy.admin", false))
                    .then(argument("player", EntityArgumentType.player())
                        .then(argument("amount", DoubleArgumentType.doubleArg(0.0))
                            .executes(command::handleTake)
                        )
                    )
                )
                .then(literal("set")
                    .requires(source -> Permissions.check(source, "mcuniversaleconomy.admin", false))
                    .then(argument("player", EntityArgumentType.player())
                        .then(argument("amount", DoubleArgumentType.doubleArg(0.0))
                            .executes(command::handleSet)
                        )
                    )
                );

            // Register command and aliases
            dispatcher.register(ecoCommand);
            dispatcher.register(literal("economy").redirect(ecoCommand.build()));
            dispatcher.register(literal("money").redirect(ecoCommand.build()));

            // Register balance command aliases
            var balanceCommand = literal("balance")
                .requires(source -> Permissions.check(source, "mcuniversaleconomy.use", true))
                .executes(ctx -> command.handleBalance(ctx, null))
                .then(argument("player", EntityArgumentType.player())
                    .requires(source -> Permissions.check(source, "mcuniversaleconomy.admin", false))
                    .executes(ctx -> command.handleBalance(ctx, EntityArgumentType.getPlayer(ctx, "player")))
                );
            dispatcher.register(balanceCommand);
            dispatcher.register(literal("bal").redirect(balanceCommand.build()));

            // Register pay command alias
            var payCommand = literal("pay")
                .requires(source -> Permissions.check(source, "mcuniversaleconomy.use", true))
                .then(argument("player", EntityArgumentType.player())
                    .then(argument("amount", DoubleArgumentType.doubleArg(0.0))
                        .executes(command::handlePay)
                    )
                );
            dispatcher.register(payCommand);
        });
    }

    private int showUsage(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendMessage(Text.literal(languageManager.getMessage("usage.eco")));
        return Command.SINGLE_SUCCESS;
    }

    private int handleBalance(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity target) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = target;

        if (player == null) {
            try {
                player = source.getPlayerOrThrow();
            } catch (Exception e) {
                source.sendMessage(Text.literal(languageManager.getMessage("balance.console_error")));
                return 0;
            }
        }

        economyCommand.balance(player.getUuid())
            .thenAccept(message -> source.sendMessage(Text.literal(message)));

        return Command.SINGLE_SUCCESS;
    }

    private int handlePay(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
            double amount = DoubleArgumentType.getDouble(ctx, "amount");

            if (player.equals(target)) {
                source.sendMessage(Text.literal(languageManager.getMessage("pay.error.self_pay")));
                return 0;
            }

            economyCommand.pay(player.getUuid(), target.getUuid(), amount)
                .thenAccept(message -> source.sendMessage(Text.literal(message)));

            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendMessage(Text.literal(languageManager.getMessage("general.command_usage")));
            return 0;
        }
    }

    private int handleGive(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        try {
            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
            double amount = DoubleArgumentType.getDouble(ctx, "amount");

            economyCommand.give(target.getUuid(), amount)
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
            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
            double amount = DoubleArgumentType.getDouble(ctx, "amount");

            economyCommand.take(target.getUuid(), amount)
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
            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
            double amount = DoubleArgumentType.getDouble(ctx, "amount");

            economyCommand.set(target.getUuid(), amount)
                .thenAccept(message -> source.sendMessage(Text.literal(message)));

            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            source.sendMessage(Text.literal(languageManager.getMessage("general.player_not_found")));
            return 0;
        }
    }
} 