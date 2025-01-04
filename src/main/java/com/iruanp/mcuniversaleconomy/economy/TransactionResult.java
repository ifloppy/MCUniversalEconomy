package com.iruanp.mcuniversaleconomy.economy;

public class TransactionResult {
    private final boolean success;
    private final String message;

    public TransactionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
} 