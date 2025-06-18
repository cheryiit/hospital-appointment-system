package com.hospital.entity;

public enum Role {
    PATIENT("Hasta"),
    DOCTOR("Doktor");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
