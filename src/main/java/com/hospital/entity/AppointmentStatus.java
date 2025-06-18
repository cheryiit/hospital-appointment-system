package com.hospital.entity;

public enum AppointmentStatus {
    PENDING("Beklemede"),
    APPROVED("Onaylandı"),
    REJECTED("Reddedildi"),
    COMPLETED("Tamamlandı"),
    CANCELLED("İptal Edildi");

    private final String displayName;

    AppointmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
