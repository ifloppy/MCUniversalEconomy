package com.iruanp.mcuniversaleconomy.economy;

public class TransactionResult {
    private final boolean success;
    private final String message;
    private final String payerName;
    private final String payeeName;

    public TransactionResult(boolean success, String message, String payerName, String payeeName) {
        this.success = success;
        this.message = message;
        this.payerName = payerName;
        this.payeeName = payeeName;
    }

    public TransactionResult(boolean success, String message) {
        this(success, message, null, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getPayerName() {
        return payerName;
    }

    public String getPayeeName() {
        return payeeName;
    }
}