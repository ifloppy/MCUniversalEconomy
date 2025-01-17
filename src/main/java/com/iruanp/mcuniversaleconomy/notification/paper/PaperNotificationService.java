package com.iruanp.mcuniversaleconomy.notification.paper;

import com.iruanp.mcuniversaleconomy.database.DatabaseManager;
import com.iruanp.mcuniversaleconomy.notification.BaseNotificationService;
import com.iruanp.mcuniversaleconomy.notification.NotificationPlayer;
import com.iruanp.mcuniversaleconomy.util.UnifiedLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PaperNotificationService extends BaseNotificationService {

    public PaperNotificationService(DatabaseManager databaseManager, UnifiedLogger logger) {
        super(databaseManager, logger);
    }

    @Override
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

    @Override
    public void sendAndRemoveNotifications(NotificationPlayer player) {
        sendAndRemoveNotificationsGeneric(player.getUniqueId(), player::sendMessage);
    }

    @Override
    public void sendAndRemoveNotificationsToAllOnlinePlayers() {
        Bukkit.getOnlinePlayers().stream()
            .filter(Player::isOnline)
            .forEach(player -> {
                UUID uuid = player.getUniqueId();
                String sql = String.format("SELECT id, message FROM %snotifications WHERE recipient_uuid = ?", prefix);
                try (Connection conn = databaseManager.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, uuid.toString());
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String message = rs.getString("message");
                        Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("MCUniversalEconomy"), () -> player.sendMessage(message));
                        deleteNotification(id);
                        logger.info("Notification sent and removed from database for UUID: " + uuid);
                    }
                } catch (SQLException e) {
                    logger.severe("Failed to send and remove notifications from database: " + e.getMessage());
                }
            });
    }
} 