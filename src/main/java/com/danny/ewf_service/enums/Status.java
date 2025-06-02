package com.danny.ewf_service.enums;


public enum Status {
    ACTIVE("active"),
    IN_TRANSIT("in_transit"),
    BROKEN("broken"),
    DEACTIVATED("deactivated");

    private final String value;

    Status(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
