package com.avaya.wiki.domain;

public enum UserStatus {
    ACTIVE("active"),
    DISABLED("disabled"),
    LOCKED("locked");

    private final String dbValue;

    UserStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    // Enum to String
    public String getDbValue() {
        return dbValue;
    }

    // String to Enum
    public static UserStatus fromDbValue(String dbValue) {
        if (dbValue == null)
            return null;
        for (UserStatus status : values()) {
            if (status.dbValue.equalsIgnoreCase(dbValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown UserStatus: " + dbValue);
    }
}

