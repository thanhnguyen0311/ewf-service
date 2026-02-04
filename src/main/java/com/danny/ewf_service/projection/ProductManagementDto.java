package com.danny.ewf_service.projection;

public interface ProductManagementDto {

    String getSku();
    String getLocalSku();
    String getUpc();
    String getAsin();
    String getShippingMethod();
    Boolean getDiscontinued();
}
