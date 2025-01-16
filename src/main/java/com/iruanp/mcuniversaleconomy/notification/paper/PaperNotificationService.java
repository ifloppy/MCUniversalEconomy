package com.iruanp.mcuniversaleconomy.notification.paper;

import com.iruanp.mcuniversaleconomy.database.DatabaseManager;
import com.iruanp.mcuniversaleconomy.notification.BaseNotificationService;
import com.iruanp.mcuniversaleconomy.notification.NotificationPlayer;
import com.iruanp.mcuniversaleconomy.util.UnifiedLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
        Bukkit.getOnlinePlayers().forEach(player -> 
            sendAndRemoveNotifications(new PaperNotificationPlayer(player))
        );
    }
} 