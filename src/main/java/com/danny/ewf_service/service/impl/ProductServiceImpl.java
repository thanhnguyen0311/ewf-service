package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.converter.IProductMapper;
import com.danny.ewf_service.entity.Product;
import com.danny.ewf_service.payload.response.ProductResponseDto;
import com.danny.ewf_service.payload.response.ProductSearchResponseDto;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final IProductMapper productMapper;

    private final ProductRepository productRepository;

    public ProductServiceImpl(IProductMapper productMapper, ProductRepository productRepository) {
        this.productMapper = productMapper;
        this.productRepository = productRepository;
    }


    @Override
    public ProductResponseDto findBySku(String sku) {
        Product product = productRepository.findBySku(sku).orElseThrow(() -> new RuntimeException("Product not found with SKU: " + sku));
        return productMapper.productToProductResponseDto(product);
    }

    @Override
    public List<ProductResponseDto> findAll() {
        List<Product> products = productRepository.findAllProducts();
        return productMapper.productListToProductResponseDtoList(products);
    }

    @Override
    public List<ProductSearchResponseDto> getAllProductsSearch() {
        List<Product> products = productRepository.findAllProducts();
        List<ProductSearchResponseDto> productSearchResponseDtoList = productMapper.productToProductSearchResponseDtoList(products);
        List<ProductSearchResponseDto> productSearchResponseDtoList2 = productMapper.productWithDifferentSkuToProductSearchResponseDtoList(products);
        productSearchResponseDtoList.addAll(productSearchResponseDtoList2);
        return productSearchResponseDtoList;
    }

    @Override
    public ProductResponseDto findById(Long id) {
        return productMapper.productToProductResponseDto(productRepository.findById(id).orElseThrow());
    }

}
