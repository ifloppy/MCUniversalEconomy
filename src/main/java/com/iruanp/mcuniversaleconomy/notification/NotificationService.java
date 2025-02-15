package com.iruanp.mcuniversaleconomy.notification;

import com.iruanp.mcuniversaleconomy.database.DatabaseManager;
import com.iruanp.mcuniversaleconomy.util.UnifiedLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class NotificationService {
    private final DatabaseManager databaseManager;
    private final UnifiedLogger logger;
    private final String prefix;

    public NotificationService(DatabaseManager databaseManager, UnifiedLogger logger) {
        this.databaseManager = databaseManager;
        this.logger = logger;
        this.prefix = databaseManager.getPrefix();
    }

    public void sendNotification(UUID recipient, String message) {
        Player player = Bukkit.getPlayer(recipient);
        if (player != null && player.isOnline()) {
            player.sendMessage(message);
            logger.info("Notification sent directly to player: " + player.getName());
        } else {
            saveNotification(recipient, message);
            logger.info("Notification saved to database for player: " + recipient);
        }
    }

    private void saveNotification(UUID recipient, String message) {
        String sql = String.format("INSERT INTO %snotifications (recipient_uuid, message) VALUES (?, ?)", prefix);
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, recipient.toString());
            stmt.setString(2, message);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Failed to save notification to database: " + e.getMessage());
        }
    }

    public void sendAndRemoveNotifications(Player player) {
        sendAndRemoveNotificationsGeneric(player.getUniqueId(), player::sendMessage);
    }

    public void sendAndRemoveNotificationsFabric(net.minecraft.server.network.ServerPlayerEntity player) {
        sendAndRemoveNotificationsGeneric(player.getUuid(), message -> player.sendMessage(net.minecraft.text.Text.literal(message)));
    }

    private void sendAndRemoveNotificationsGeneric(UUID uuid, java.util.function.Consumer<String> messageSender) {
        String sql = String.format("SELECT id, message FROM %snotifications WHERE recipient_uuid = ?", prefix);
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String message = rs.getString("message");
                messageSender.accept(message);
                deleteNotification(id);
                logger.info("Notification sent and removed from database for UUID: " + uuid);
            }
        } catch (SQLException e) {
            logger.severe("Failed to send and remove notifications from database: " + e.getMessage());
        }
    }

    public void sendAndRemoveNotificationsToAllOnlinePlayers() {
        Bukkit.getOnlinePlayers().forEach(this::sendAndRemoveNotifications);
    }

    private void deleteNotification(int id) {
        String sql = String.format("DELETE FROM %snotifications WHERE id = ?", prefix);
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Failed to delete notification from database: " + e.getMessage());
        }
    }
}