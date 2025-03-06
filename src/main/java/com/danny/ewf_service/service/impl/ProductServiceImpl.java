package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.converter.IProductMapper;
import com.danny.ewf_service.entity.LocalProduct;
import com.danny.ewf_service.entity.Product;
import com.danny.ewf_service.entity.ProductComponent;
import com.danny.ewf_service.payload.response.ProductResponseDto;
import com.danny.ewf_service.payload.response.ProductSearchResponseDto;
import com.danny.ewf_service.repository.LocalRepository;
import com.danny.ewf_service.repository.ProductComponentRepository;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.service.ProductService;
import com.danny.ewf_service.utils.imports.SKUGenerator;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {

    @Autowired
    private final SKUGenerator skuGenerator;

    private final LocalRepository localRepository;

    private final IProductMapper productMapper;

    private final ProductRepository productRepository;

    private final ProductComponentRepository productComponentRepository;

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

    @Override
    public List<Product> findMergedProducts(Product product) {

        List<ProductComponent> groupComponents = product.getProductComponents().stream()
                .filter(pc -> "Group".equalsIgnoreCase(pc.getComponent().getType()))
                .toList();
        if (groupComponents.size() < 2) {
            return null; // No merged product exists
        }
        List<Product> mergedProducts = new ArrayList<>();

        for (int size = 2; size <= groupComponents.size(); size++) {
            List<List<ProductComponent>> combinations = generateCombinations(groupComponents, size);

            // Check if a product exists for each combination
            for (List<ProductComponent> combination : combinations) {
                // Extract SKUs of components in the combination
                List<String> componentSkus = combination.stream()
                        .map(pc -> pc.getComponent().getSku())
                        .collect(Collectors.toList());
                Optional<Object[]> result = productComponentRepository.findProductByExactComponents(componentSkus, componentSkus.size());
                if (result.isPresent() && result.get().length > 0) {
                    Object[] resultArray = result.get();
                    if (resultArray[0] instanceof Object[] firstRow) {
                        if (firstRow.length > 1) {
                            Object sku = firstRow[1];
                            Optional<Product> productOptional = productRepository.findBySku((String) sku);
                            productOptional.ifPresent(mergedProducts::add);
                        }
                    }
                }
            }
        }

        return mergedProducts;
    }

    private List<List<ProductComponent>> generateCombinations(List<ProductComponent> components, int size) {
        List<List<ProductComponent>> combinations = new ArrayList<>();
        generateCombinationsRecursive(components, size, 0, new ArrayList<>(), combinations);
        return combinations;
    }

    private void generateCombinationsRecursive(
            List<ProductComponent> components,
            int size,
            int startIndex,
            List<ProductComponent> currentCombination,
            List<List<ProductComponent>> combinations
    ) {
        if (currentCombination.size() > 3) {
            return;
        }

        if (currentCombination.size() == size) {
            combinations.add(new ArrayList<>(currentCombination));
            return;
        }

        for (int i = startIndex; i < components.size(); i++) {
            currentCombination.add(components.get(i));
            generateCombinationsRecursive(components, size, i + 1, currentCombination, combinations);
            currentCombination.remove(currentCombination.size() - 1); // Backtrack
        }
    }
}

