package com.iruanp.mcuniversaleconomy.platform;

public enum PlatformType {
    FABRIC,
    PAPER;

    private static PlatformType currentPlatform;

    public static PlatformType getCurrentPlatform() {
        if (currentPlatform == null) {
            detectPlatform();
        }
        return currentPlatform;
    }

    private static void detectPlatform() {
        try {
            // Check for Paper
            Class.forName("org.bukkit.plugin.java.JavaPlugin");
            currentPlatform = PAPER;
        } catch (ClassNotFoundException e) {
            // Must be Fabric
            currentPlatform = FABRIC;
        }
    }

    public static boolean isPaper() {
        return getCurrentPlatform() == PAPER;
    }

    public static boolean isFabric() {
        return getCurrentPlatform() == FABRIC;
    }
} 