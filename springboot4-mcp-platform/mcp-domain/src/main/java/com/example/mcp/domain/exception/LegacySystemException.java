package com.example.mcp.domain.exception;

/**
 * 旧系统调用失败时抛出的统一异常。
 */
public class LegacySystemException extends RuntimeException {

    private final int statusCode;
    private final String legacyCode;

    public LegacySystemException(int statusCode, String message, String legacyCode) {
        super(message);
        this.statusCode = statusCode;
        this.legacyCode = legacyCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getLegacyCode() {
        return legacyCode;
    }
}
