package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.converter.IProductMapper;
import com.danny.ewf_service.entity.product.Product;
import org.springframework.beans.factory.annotation.Qualifier;
import com.danny.ewf_service.entity.product.ProductComponent;
import com.danny.ewf_service.payload.response.ProductDetailResponseDto;
import com.danny.ewf_service.payload.response.ProductResponseDto;
import com.danny.ewf_service.payload.response.ProductSearchResponseDto;
import com.danny.ewf_service.repository.ProductComponentRepository;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.service.CacheService;
import com.danny.ewf_service.service.ComponentService;
import com.danny.ewf_service.service.InventoryService;
import com.danny.ewf_service.service.ProductService;
import com.danny.ewf_service.utils.imports.SKUGenerator;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {

    @Autowired
    private final SKUGenerator skuGenerator;

    private final IProductMapper productMapper;

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final ComponentService componentService;

    @Autowired
    private final ProductComponentRepository productComponentRepository;

    @Autowired
    private final CacheService cacheService;

    @Autowired
    private InventoryService inventoryService;

    public interface ProductMergedProjection {
        Long getId();
        String getSku();
    }

    @Override
    public ProductResponseDto findBySku(String sku) {
        Product product = productRepository.findProductBySku(sku.toUpperCase()).orElseThrow(() -> new RuntimeException("Product not found with sku: " + sku));
        ProductResponseDto productResponseDto = productMapper.productToProductResponseDto(product);
        productResponseDto.setInventory(inventoryService.getInventoryProductCountById(product.getId()));
        productResponseDto.setComponents(componentService.findComponents(product));
        List<Product> subProductResponseDtoList = findMergedProducts(product);
        List<ProductResponseDto> subProductResponseDtos = new ArrayList<>();
        ProductResponseDto subProductResponseDto;
        if (subProductResponseDtoList != null) {
            for (Product subProduct : subProductResponseDtoList) {
                subProductResponseDto = productMapper.productToProductResponseDto(subProduct);
                subProductResponseDto.setInventory(inventoryService.getInventoryProductCountById(subProduct.getId()));
                subProductResponseDto.setComponents(componentService.findComponents(subProduct));
                subProductResponseDto.setInventory(productComponentRepository.countByProductId(subProduct.getId()));
                subProductResponseDtos.add(subProductResponseDto);
            }
        }
        List<String> subSingleSkus = productComponentRepository.findSingleProductsByProductSku(product.getId());
        Product subProduct;
        for (String subSingleSku : subSingleSkus) {
            if (subSingleSku.equals(product.getSku())) continue;
            Optional<Product> subProductOptional = productRepository.findProductBySku(subSingleSku);
            if (subProductOptional.isPresent()) {
                subProduct = subProductOptional.get();
                subProductResponseDto = productMapper.productToProductResponseDto(subProduct);
                subProductResponseDto.setInventory(inventoryService.getInventoryProductCountById(subProduct.getId()));
                subProductResponseDtos.add(subProductResponseDto);
            }
        }
        productResponseDto.setSubProducts(subProductResponseDtos);
        return productResponseDto;
    }

    @Override
    public List<ProductDetailResponseDto> findAllProductsToDtos() {
//        List<ProductProjection> products = productRepository.findAllProductDetails();
//        return productMapper.productProjectionToProductDetailResponseDtoList(products);
        List<Product> products = cacheService.getAllProducts();
        return productMapper.productListToProductDetailResponseDtoList(products);
    }

    @Override
    @Cacheable(value = "productsCache", key = "#productId")
    public Product getProductById(Long productId) {
        return productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found: " + productId));
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
    @Transactional
    @CacheEvict(value = "productsCache", key = "#product.id")
    public Product saveProduct(Product product) {
        Product updatedProduct = productRepository.save(product);

        cacheService.reloadProductInCache(updatedProduct);

        return updatedProduct;
    }

    @Override
    public List<Product> findMergedProducts(Product product) {

        List<ProductComponent> groupComponents = product.getComponents().stream()
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
                List<Long> componentIds = combination.stream()
                        .map(pc -> pc.getComponent().getId())
                        .collect(Collectors.toList());

                Optional<ProductMergedProjection> result = productComponentRepository.findProductByExactComponents(componentIds, componentIds.size());

                if (result.isPresent()) {
                    ProductMergedProjection productProjection = result.get();

                    if (productProjection.getSku().equals(product.getSku())) continue;

                    Optional<Product> productOptional = productRepository.findProductBySku(productProjection.getSku());
                    productOptional.ifPresent(mergedProducts::add);
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

