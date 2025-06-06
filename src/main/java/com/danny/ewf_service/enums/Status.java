package com.danny.ewf_service.enums;


import lombok.Getter;

@Getter
public enum Status {
    ACTIVE("active"),
    IN_TRANSIT("in_transit"),
    BROKEN("broken"),
    DEACTIVATED("deactivated");

    private final String value;

    Status(String value) {
        this.value = value;
    }

}
