package com.iruanp.mcuniversaleconomy.notification.fabric;

import com.iruanp.mcuniversaleconomy.notification.NotificationPlayer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

public class FabricNotificationPlayer implements NotificationPlayer {
    private final ServerPlayerEntity player;

    public FabricNotificationPlayer(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public UUID getUniqueId() {
        return player.getUuid();
    }

    @Override
    public String getName() {
        return player.getName().getString();
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(Text.literal(message));
    }

    @Override
    public boolean isOnline() {
        return !player.isDisconnected();
    }
} 