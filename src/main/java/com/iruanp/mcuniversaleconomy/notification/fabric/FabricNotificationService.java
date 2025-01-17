package com.iruanp.mcuniversaleconomy.notification.fabric;

import com.iruanp.mcuniversaleconomy.database.DatabaseManager;
import com.iruanp.mcuniversaleconomy.notification.BaseNotificationService;
import com.iruanp.mcuniversaleconomy.notification.NotificationPlayer;
import com.iruanp.mcuniversaleconomy.util.UnifiedLogger;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
        List<ServerPlayerEntity> players = new ArrayList<>(server.getPlayerManager().getPlayerList());
        players.stream()
            .filter(player -> !player.isDisconnected())
            .forEach(player -> {
                UUID uuid = player.getUuid();
                String sql = String.format("SELECT id, message FROM %snotifications WHERE recipient_uuid = ?", prefix);
                try (Connection conn = databaseManager.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, uuid.toString());
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String message = rs.getString("message");
                        server.execute(() -> player.sendMessage(Text.literal(message)));
                        deleteNotification(id);
                        logger.info("Notification sent and removed from database for UUID: " + uuid);
                    }
                } catch (SQLException e) {
                    logger.severe("Failed to send and remove notifications from database: " + e.getMessage());
                }
            });
    }
} 