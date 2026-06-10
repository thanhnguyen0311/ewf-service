package com.danny.ewf_service.payload.projection;

import com.danny.ewf_service.entity.product.ProductComponent;

import java.util.List;

public interface ProductManagementDto {

    String getSku();
    Long getProductId();
    String getLocalSku();
    String getUpc();
    String getAsin();
    String getShippingMethod();
    Boolean getDiscontinued();
    String getSubCategory();
    String getMainCategory();
    String getCategory();
    String getFinish();
    String getSizeShape();
    String getCollection();
    String getShortTitle();
    String getStyle();
    String getTitle();
    String getType();
    String getProductType();
    String getIsLuxe();
    String getGroupID();
    String getPieces();
    String getChairType();
    List<String> getSubProducts();

}
