package com.iruanp.mcuniversaleconomy.notification;

import com.iruanp.mcuniversaleconomy.database.DatabaseManager;
import com.iruanp.mcuniversaleconomy.util.UnifiedLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class BaseNotificationService {
    protected final DatabaseManager databaseManager;
    protected final UnifiedLogger logger;
    protected final String prefix;

    protected BaseNotificationService(DatabaseManager databaseManager, UnifiedLogger logger) {
        this.databaseManager = databaseManager;
        this.logger = logger;
        this.prefix = databaseManager.getPrefix();
    }

    protected void saveNotification(UUID recipient, String message) {
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

    protected void sendAndRemoveNotificationsGeneric(UUID uuid, Consumer<String> messageSender) {
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

    public abstract void sendNotification(UUID recipient, String message);
    public abstract void sendAndRemoveNotifications(NotificationPlayer player);
    public abstract void sendAndRemoveNotificationsToAllOnlinePlayers();
} 