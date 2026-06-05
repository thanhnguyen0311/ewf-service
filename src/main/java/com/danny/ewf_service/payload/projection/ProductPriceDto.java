package com.danny.ewf_service.payload.projection;

public interface ProductPriceDto {
    String getSku();
    Double getTotalQB();
    Double getShippingCost();
    Double getManualShippingCost();
    Double getPromotion();
    Double getEwfdirectManualPrice();

    Double getAmazonPrice();
}
