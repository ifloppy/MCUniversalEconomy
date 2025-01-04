package com.iruanp.mcuniversaleconomy.util;

import org.slf4j.Logger;
import java.util.logging.Level;

public class UnifiedLogger {
    private final Logger slf4jLogger;
    private final java.util.logging.Logger javaLogger;

    public UnifiedLogger(Logger slf4jLogger) {
        this.slf4jLogger = slf4jLogger;
        this.javaLogger = null;
    }

    public UnifiedLogger(java.util.logging.Logger javaLogger) {
        this.javaLogger = javaLogger;
        this.slf4jLogger = null;
    }

    public void info(String message) {
        if (slf4jLogger != null) {
            slf4jLogger.info(message);
        } else {
            javaLogger.info(message);
        }
    }

    public void error(String message) {
        if (slf4jLogger != null) {
            slf4jLogger.error(message);
        } else {
            javaLogger.log(Level.SEVERE, message);
        }
    }

    public void error(String message, Throwable throwable) {
        if (slf4jLogger != null) {
            slf4jLogger.error(message, throwable);
        } else {
            javaLogger.log(Level.SEVERE, message, throwable);
        }
    }

    public void severe(String message) {
        if (slf4jLogger != null) {
            slf4jLogger.error(message);
        } else {
            javaLogger.severe(message);
        }
    }

    public void severe(String message, Throwable throwable) {
        if (slf4jLogger != null) {
            slf4jLogger.error(message, throwable);
        } else {
            javaLogger.log(Level.SEVERE, message, throwable);
        }
    }
} 