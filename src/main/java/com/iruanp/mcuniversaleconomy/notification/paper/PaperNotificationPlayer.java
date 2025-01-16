package com.iruanp.mcuniversaleconomy.notification.paper;

import com.iruanp.mcuniversaleconomy.notification.NotificationPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PaperNotificationPlayer implements NotificationPlayer {
    private final Player player;

    public PaperNotificationPlayer(Player player) {
        this.player = player;
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(message);
    }

    @Override
    public boolean isOnline() {
        return player.isOnline();
    }
} 