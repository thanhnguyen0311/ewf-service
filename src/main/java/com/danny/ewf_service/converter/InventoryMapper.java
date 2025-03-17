package com.danny.ewf_service.converter;

import com.danny.ewf_service.entity.Product;
import com.danny.ewf_service.payload.response.ProductInventoryResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryMapper {


    ProductInventoryResponseDto productToProductInventoryResponseDto(Product product);
    List<ProductInventoryResponseDto> productListToProductInventoryResponseDtoList(List<Product> products);
}
