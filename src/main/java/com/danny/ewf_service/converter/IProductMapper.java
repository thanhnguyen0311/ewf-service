package com.danny.ewf_service.converter;


import com.danny.ewf_service.entity.Product;
import com.danny.ewf_service.payload.response.ProductResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface IProductMapper {
    IProductMapper INSTANCE = Mappers.getMapper(IProductMapper.class);

    @Mapping(target = "id", source="product.id")
    @Mapping(target = "sku", source="product.sku")
    @Mapping(target = "price", source="product.price")
    @Mapping(target = "localPrice", source="product.localProduct.price")
    @Mapping(target = "images", source="product.images")
    ProductResponseDto productToProductResponseDto(Product product);
}
