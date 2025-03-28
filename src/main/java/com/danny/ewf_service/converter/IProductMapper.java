package com.danny.ewf_service.converter;

import com.danny.ewf_service.entity.ImageUrls;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.entity.product.ProductComponent;
import com.danny.ewf_service.payload.response.ProductDetailResponseDto;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface IProductMapper {
    ObjectMapper OBJECT_MAPPER = new ObjectMapper(); // For JSON parsing

    @Mapping(target = "id", source="product.id")
    @Mapping(target = "sku", source="product.sku")
    @Mapping(target = "localSku", source="product.localSku")
    @Mapping(target = "images", source="product.images", qualifiedByName = "extractImages")
    @Mapping(target = "finish", source = "product.productDetail.finish")
    ProductResponseDto productToProductResponseDto(Product product);
    List<ProductResponseDto> productListToProductResponseDtoList(List<Product> products);

    @Named("productToSearchResponse")
    @Mapping(target = "id", source = "product.id")
    @Mapping(target = "sku", source = "product.sku")
    @Mapping(target = "image", source = "product.images", qualifiedByName = "extractFirstImage")
    ProductSearchResponseDto productToProductSearchResponseDto(Product product);
    @IterableMapping(qualifiedByName = "productToSearchResponse")
    List<ProductSearchResponseDto> productToProductSearchResponseDtoList(List<Product> products);

    @Named("productWithDifferentSkuToSearchResponse")
    @Mapping(target = "id", source = "product.id")
    @Mapping(target = "sku", source = "product.localSku") // Override the SKU
    @Mapping(target = "image", source = "product.images", qualifiedByName = "extractFirstImage")
    ProductSearchResponseDto productWithDifferentSkuToProductSearchResponseDto(Product product);

    @IterableMapping(qualifiedByName = "productWithDifferentSkuToSearchResponse")
    List<ProductSearchResponseDto> productWithDifferentSkuToProductSearchResponseDtoList(List<Product> products);

    @Mapping(target = "id", source = "product.id")
    @Mapping(target = "sku", source = "product.sku")
    ProductInventoryResponseDto productToProductInventoryResponseDto(Product product);
    List<ProductInventoryResponseDto> productListToProductInventoryResponseDtoList(List<Product> products);


    @Mapping(target = "id", source = "product.id")
    @Mapping(target = "sku", source = "product.sku")
    @Mapping(target = "localSku", source = "product.localSku")
    @Mapping(target = "image", source = "product.images", qualifiedByName = "extractFirstImage")
    @Mapping(target = "upc", source = "product.upc")
    @Mapping(target = "asin", source = "product.asin")
    @Mapping(target = "title", source = "product.title")
    @Mapping(target = "localTitle", source = "product.localTitle")
    @Mapping(target = "description", source = "product.productDetail.description")
    @Mapping(target = "shippingMethod", source = "product.shippingMethod")
    @Mapping(target = "pieces", source = "product.productDetail.pieces")
    @Mapping(target = "discontinued", source = "product.discontinued")
    @Mapping(target = "components", source = "product.productComponents", qualifiedByName = "listComponents")
    ProductDetailResponseDto productToProductDetailResponseDto(Product product);
    List<ProductDetailResponseDto> productListToProductDetailResponseDtoList(List<Product> products);


    @Named("extractImages")
    default ImageUrls extractImages(String imagesJson) {
        return new ImageUrls().parseImageJson(imagesJson);
    }

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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Named("listComponents")
    default List<Map<String,Long>> listComponents(List<ProductComponent> components) {
        List<Map<String, Long>> componentList = new ArrayList<>();
        for (ProductComponent component : components) {
            componentList.add(Map.of("id", component.getId(), component.getComponent().getSku(), component.getQuantity()));
        }
        return componentList;
    }

}

