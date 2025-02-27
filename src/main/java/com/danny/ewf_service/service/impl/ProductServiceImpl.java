package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.converter.IProductMapper;
import com.danny.ewf_service.entity.LocalProduct;
import com.danny.ewf_service.entity.Product;
import com.danny.ewf_service.payload.response.ProductResponseDto;
import com.danny.ewf_service.payload.response.ProductSearchResponseDto;
import com.danny.ewf_service.repository.LocalRepository;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.service.ProductService;
import com.danny.ewf_service.utils.imports.SKUGenerator;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {

    @Autowired
    private final SKUGenerator skuGenerator;

    private final LocalRepository localRepository;

    private final IProductMapper productMapper;

    private final ProductRepository productRepository;

    @Override
    public ProductResponseDto findBySku(String sku) {
        Product product = productRepository.findProductBySku(sku).orElseThrow(() -> new RuntimeException("Product not found with SKU: " + sku));
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


    @Override
    @Transactional
    public void saveProduct(Product product) {
        if (product.getLocalProduct() == null) {
            LocalProduct localProduct = new LocalProduct();
            localProduct.setLocalSku(skuGenerator.generateNewSKU(product.getSku()));
            localRepository.save(localProduct);
            System.out.println("\u001B[32m" + "Successfully created Local SKU : " + localProduct.getLocalSku() + "\u001B[0m");
            product.setLocalProduct(localProduct);
        }
        productRepository.save(product);
    }
}
