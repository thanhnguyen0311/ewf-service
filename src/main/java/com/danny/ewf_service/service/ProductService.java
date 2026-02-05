package com.danny.ewf_service.service;

import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.payload.projection.ProductComponentDto;
import com.danny.ewf_service.payload.projection.ProductManagementDto;
import com.danny.ewf_service.payload.request.product.ProductDetailRequestDto;
import com.danny.ewf_service.payload.response.product.ProductDetailResponseDto;
import com.danny.ewf_service.payload.response.product.ProductPriceResponseDto;
import com.danny.ewf_service.payload.response.product.ProductResponseDto;
import com.danny.ewf_service.payload.response.product.ProductSearchResponseDto;

import java.util.List;

public interface ProductService {

    ProductResponseDto findBySku(String sku);

    List<ProductDetailResponseDto> findAllProductsToDtos();

    List<ProductSearchResponseDto> getAllProductsSearch();

    List<Product> findMergedProducts(Product product);

    ProductDetailResponseDto updateProductDetailById(Long id, ProductDetailRequestDto productDetailRequestDto);

    List<String> getAllImagesProduct(Product product);

    double calculateEWFDirectPriceGround(Product product, List<String[]> rows);

    double calculateEWFDirectPriceLTL(Product product, List<String[]> rows);

    ProductPriceResponseDto getProductPrice(String sku);

    void calculateProductPrice();

    List<Product> getListProductFromCsvFile(String filePath);


    List<ProductManagementDto> getAllProductManagementDtos();
    List<ProductComponentDto> getAllProductComponentDtos();
}
