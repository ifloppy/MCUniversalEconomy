package com.iruanp.mcuniversaleconomy.notification;

import java.util.UUID;

public interface NotificationPlayer {
    UUID getUniqueId();
    String getName();
    void sendMessage(String message);
    boolean isOnline();
} 