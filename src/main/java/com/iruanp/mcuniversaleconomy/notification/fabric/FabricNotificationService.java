package com.iruanp.mcuniversaleconomy.notification.fabric;

import com.iruanp.mcuniversaleconomy.database.DatabaseManager;
import com.iruanp.mcuniversaleconomy.notification.BaseNotificationService;
import com.iruanp.mcuniversaleconomy.notification.NotificationPlayer;
import com.iruanp.mcuniversaleconomy.util.UnifiedLogger;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

public class FabricNotificationService extends BaseNotificationService {
    private final MinecraftServer server;

    public FabricNotificationService(DatabaseManager databaseManager, UnifiedLogger logger, MinecraftServer server) {
        super(databaseManager, logger);
        this.server = server;
    }

    @Override
    public void sendNotification(UUID recipient, String message) {
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(recipient);
        if (player != null && !player.isDisconnected()) {
            player.sendMessage(Text.literal(message));
            logger.info("Notification sent directly to player: " + player.getName().getString());
        } else {
            saveNotification(recipient, message);
            logger.info("Notification saved to database for player: " + recipient);
        }
    }

    @Override
    public void sendAndRemoveNotifications(NotificationPlayer player) {
        sendAndRemoveNotificationsGeneric(player.getUniqueId(), player::sendMessage);
    }

    @Override
    public void sendAndRemoveNotificationsToAllOnlinePlayers() {
        server.getPlayerManager().getPlayerList().forEach(player ->
            sendAndRemoveNotifications(new FabricNotificationPlayer(player))
        );
    }
} 