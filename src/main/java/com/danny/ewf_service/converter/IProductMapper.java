package com.danny.ewf_service.converter;


import com.danny.ewf_service.entity.ImageUrls;
import com.danny.ewf_service.entity.Product;
import com.danny.ewf_service.payload.response.ProductResponseDto;
import com.danny.ewf_service.payload.response.ProductSearchResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface IProductMapper {
    IProductMapper INSTANCE = Mappers.getMapper(IProductMapper.class);
    ObjectMapper OBJECT_MAPPER = new ObjectMapper(); // For JSON parsing


    @Mapping(target = "id", source="product.id")
    @Mapping(target = "sku", source="product.sku")
    @Mapping(target = "price", source="product.price")
    @Mapping(target = "localPrice", source="product.localProduct.price")
    @Mapping(target = "images", source="product.images")
    ProductResponseDto productToProductResponseDto(Product product);
    List<ProductResponseDto> productListToProductResponseDtoList(List<Product> products);

    @Named("productToSearchResponse")
    @Mapping(target = "id", source = "product.id")
    @Mapping(target = "sku", source = "product.sku")
    @Mapping(target = "price", source = "product.price")
    @Mapping(target = "image", source = "product.images", qualifiedByName = "extractFirstImage")
    @Mapping(target = "finish", source = "product.finish")
    ProductSearchResponseDto productToProductSearchResponseDto(Product product);
    @IterableMapping(qualifiedByName = "productToSearchResponse")
    List<ProductSearchResponseDto> productToProductSearchResponseDtoList(List<Product> products);

    @Named("productWithDifferentSkuToSearchResponse")
    @Mapping(target = "id", source = "product.id")
    @Mapping(target = "sku", source = "product.localProduct.localSku") // Override the SKU
    @Mapping(target = "price", source = "product.localProduct.price")
    @Mapping(target = "image", source = "product.images", qualifiedByName = "extractFirstImage")
    @Mapping(target = "finish", source = "product.finish")
    ProductSearchResponseDto productWithDifferentSkuToProductSearchResponseDto(Product product);
    @IterableMapping(qualifiedByName = "productWithDifferentSkuToSearchResponse")
    List<ProductSearchResponseDto> productWithDifferentSkuToProductSearchResponseDtoList(List<Product> products);


    @Named("extractFirstImage")
    default String extractFirstImage(String imagesJson) {
        try {
            if (imagesJson == null || imagesJson.isEmpty()) {
                return "";
            }

            JsonNode jsonNode = OBJECT_MAPPER.readTree(imagesJson);

            if (jsonNode.isArray()) {
                for (JsonNode node : jsonNode) {
                    String imgLink = node.asText();
                    if (imgLink.contains("/DNS/")) {
                        return imgLink; // Return the first image containing "/DNS/"
                    }
                }
                return jsonNode.get(0).asText(); // Fallback to the first image
            }

            // Case 2: If the input is an object, map it to `ImageUrls` and process
            ImageUrls imageUrls = OBJECT_MAPPER.convertValue(jsonNode, ImageUrls.class);

            if (imageUrls.getImg() != null && !imageUrls.getImg().isEmpty()) {
                for (String imgLink : imageUrls.getImg()) {
                    if (imgLink.contains("/DNS/")) {
                        return imgLink; // Return the first match
                    }
                }
                return imageUrls.getImg().get(0); // Fallback to the first image
            }

            if (imageUrls.getDim() != null && !imageUrls.getDim().isEmpty()) {
                for (String dimLink : imageUrls.getDim()) {
                    if (dimLink.contains("/DNS/")) {
                        return dimLink; // Return the first match
                    }
                }
                return imageUrls.getDim().get(0); // Fallback to the first dimension
            }
        } catch (JsonProcessingException e) {
            return "";
        }
        return "";
    }
}

