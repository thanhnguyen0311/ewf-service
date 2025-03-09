package com.danny.ewf_service.converter;

import com.danny.ewf_service.entity.ImageUrls;
import com.danny.ewf_service.entity.Product;
import com.danny.ewf_service.payload.response.ProductInventoryResponseDto;
import com.danny.ewf_service.payload.response.ProductResponseDto;
import com.danny.ewf_service.payload.response.ProductSearchResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IProductMapper {
    ObjectMapper OBJECT_MAPPER = new ObjectMapper(); // For JSON parsing

    @Mapping(target = "id", source="product.id")
    @Mapping(target = "sku", source="product.sku")
    @Mapping(target = "localSku", source="product.localProduct.localSku")
    @Mapping(target = "images", source="product.images")
    @Mapping(target = "finish", source = "product.finish")
    @Mapping(target = "category", source = "product.category")
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
            if (imagesJson == null || imagesJson.trim().isEmpty()) {
                return "";
            }

            JsonNode jsonNode = OBJECT_MAPPER.readTree(imagesJson);

            if (jsonNode.isArray()) {
                for (JsonNode node : jsonNode) {
                    if (node != null && node.asText().contains("/DNS/")) {
                        return node.asText();
                    }
                }
                return jsonNode.has(0) ? jsonNode.get(0).asText("") : "";
            }


            if (jsonNode.isObject()) {
                ImageUrls imageUrls = OBJECT_MAPPER.convertValue(jsonNode, ImageUrls.class);

                if (imageUrls.getImg() != null && !imageUrls.getImg().isEmpty()) {
                    for (String imgLink : imageUrls.getImg()) {
                        if (imgLink.contains("/DNS/")) {
                            return imgLink;
                        }
                    }
                    return imageUrls.getImg().get(0);
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }


    @Mapping(target = "image", source = "product.images", qualifiedByName = "extractFirstImage")
    @Mapping(target = "id", source = "product.id")
    @Mapping(target = "sku", source = "product.sku")
    ProductInventoryResponseDto productToProductInventoryResponseDto(Product product);
    List<ProductInventoryResponseDto> productListToProductInventoryResponseDtoList(List<Product> products);


}

