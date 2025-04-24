package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.converter.IProductMapper;
import com.danny.ewf_service.entity.Dimension;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.entity.product.ProductDetail;
import com.danny.ewf_service.entity.product.ProductWholesales;
import com.danny.ewf_service.payload.request.ProductDetailRequestDto;
import com.danny.ewf_service.entity.product.ProductComponent;
import com.danny.ewf_service.payload.response.product.ProductDetailResponseDto;
import com.danny.ewf_service.payload.response.product.ProductResponseDto;
import com.danny.ewf_service.payload.response.product.ProductSearchResponseDto;
import com.danny.ewf_service.repository.ProductComponentRepository;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.service.CacheService;
import com.danny.ewf_service.service.ComponentService;
import com.danny.ewf_service.service.InventoryService;
import com.danny.ewf_service.service.ProductService;
import com.danny.ewf_service.utils.imports.SKUGenerator;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.*;
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
        List<Product> products = cacheService.getAllProducts();
        return productMapper.productListToProductDetailResponseDtoList(products);
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
    public ProductDetailResponseDto updateProductDetailById(Long id, ProductDetailRequestDto productDetailRequestDto) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        updateProductFromDto(product, productDetailRequestDto);
        Product savedProduct = cacheService.saveProduct(product);
        return productMapper.productToProductDetailResponseDto(savedProduct);
    }

    @Override
    public List<String> getAllImagesProduct(Product product) {

        return List.of();
    }

    @Override
    public double calculateEWFDirectPriceGround(Product product,  List<String[]> rows) {
        double productWeight = 0;
        double productPrice = 0;
        double totalShipCost = 0;
        double totalQB1 = 0;
        int stt = 1;
        long totalQuantity = 0;

        List<ProductComponent> components = product.getComponents();

        for (ProductComponent productComponent : components) {
            double shippingCost = 0;
            double girth = 0;
            Dimension dimension = productComponent.getComponent().getDimension();
            long quantityBox = 0;
            double componentPrice = productComponent.getComponent().getPrice().getQB3();
            double boxCount = 0;
            if (dimension != null) {

                quantityBox = productComponent.getComponent().getDimension().getQuantityBox();

                if (quantityBox == 0) {
                    quantityBox = 1;
                }

                boxCount = (double) productComponent.getQuantity() / quantityBox;
                double componentWeight = (dimension.getBoxLength() * dimension.getBoxWidth() * dimension.getBoxHeight()) / 139;

                if (componentWeight < dimension.getBoxWeight()) {
                    componentWeight = dimension.getBoxWeight();
                }
                productWeight = productWeight + componentWeight;

                if (componentWeight <= 20) {
                    shippingCost = 18;
                } else if (componentWeight <= 40) {
                    shippingCost = 25;
                } else if (componentWeight <= 50) {
                    shippingCost = 27;
                } else if (componentWeight <= 60) {
                    shippingCost = 30;
                } else if (componentWeight <= 70) {
                    shippingCost = 32;
                } else if (componentWeight <= 80) {
                    shippingCost = 34;
                } else {
                    shippingCost = 35;
                }


                girth = dimension.getBoxLength() + 2*(dimension.getBoxWidth() + dimension.getBoxHeight());
                if (girth > 118) {
                    shippingCost = shippingCost + 115;
                    if (girth > 165 && product.getComponents().size() == 1) {
                        shippingCost = shippingCost + 50;
                    }
                } else {
                    if (dimension.getBoxLength() >= 44) {
                        shippingCost = shippingCost + 30;
                    }
                }

                totalQB1 = totalQB1 + (componentPrice*productComponent.getQuantity());
                shippingCost = shippingCost * boxCount;
                totalShipCost = totalShipCost + shippingCost;
                totalQuantity = totalQuantity + productComponent.getQuantity();
                rows.add(new String[]{
                        String.valueOf(stt),
                        "",
                        "",
                        product.getShippingMethod(),
                        "",
                        productComponent.getComponent().getSku(),
                        String.valueOf(componentWeight),
                        String.valueOf(girth),
                        String.valueOf(productComponent.getQuantity()),
                        String.valueOf(productComponent.getComponent().getPrice().getQB3()),
                        String.valueOf(shippingCost),
                        String.valueOf(componentPrice*productComponent.getQuantity() +shippingCost),
                });
                stt++;
            }
        }
        if (Objects.equals(product.getShippingMethod(), "LTL")){
            if (totalShipCost > 500) {
                totalShipCost = totalShipCost * 0.8;
            } else if (totalShipCost > 400) {
                totalShipCost = totalShipCost * 0.85;
            } else if (totalShipCost > 300) {
                totalShipCost = totalShipCost * 0.9;
            }
        }

//        if (totalQB1 > 2000) {
//            totalQB1 = totalQB1 * 0.9;
//        } else if (totalQB1 > 1000) {
//            totalQB1 = totalQB1 * 0.95;
//        } else if (totalQB1 > 500) {
//            totalQB1 = totalQB1 * 0.95;
//        }
        productPrice = totalQB1 + totalShipCost;


        product.getPrice().setEwfdirect(productPrice);
        productRepository.save(product);

        rows.add(new String[]{
                String.valueOf(stt),
                product.getSku().toLowerCase(),
                product.getTitle(),
                String.valueOf(product.getShippingMethod()),
                String.valueOf(productPrice),
                "","","",
                String.valueOf(totalQuantity),
                String.valueOf(totalQB1),
                String.valueOf(totalShipCost),
                String.valueOf(productPrice),
                String.valueOf(product.getPrice().getAmazonPrice()),
                "http://www.amazon.com/dp/"+product.getAsin(),
        });
        return productPrice;
    }

    @Override
    public double calculateEWFDirectPriceLTL(Product product, List<String[]> rows) {
        return 0;
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

    private void updateProductFromDto(Product product, ProductDetailRequestDto dto) {
        if (dto.getTitle() != null) product.setTitle(dto.getTitle());
        if (dto.getLocalTitle() != null) product.setLocalTitle(dto.getLocalTitle());
        if (dto.getUpc() != null) product.setUpc(dto.getUpc());
        if (dto.getAsin() != null) product.setAsin(dto.getAsin());
        if (dto.getType() != null) product.setType(dto.getType());
        if (dto.getOrder() != null) product.setOrder(dto.getOrder());
        if (dto.getCategory() != null) product.setCategory(dto.getCategory());
        if (dto.getShippingMethod() != null) product.setShippingMethod(dto.getShippingMethod());
        if (dto.getDiscontinued() != null) product.setDiscontinued(dto.getDiscontinued());


        // Initialize productDetail if null
        if (product.getProductDetail() == null) {
            product.setProductDetail(new ProductDetail());
        }

        // Update productDetail fields
        if (dto.getDescription() != null) product.getProductDetail().setDescription(dto.getDescription());
        if (dto.getHtmlDescription() != null) product.getProductDetail().setHtmlDescription(dto.getHtmlDescription());
        if (dto.getMainCategory() != null) product.getProductDetail().setMainCategory(dto.getMainCategory());
        if (dto.getCollection() != null) product.getProductDetail().setCollection(dto.getCollection());
        if (dto.getSubCategory() != null) product.getProductDetail().setSubCategory(dto.getSubCategory());
        if (dto.getPieces() != null) product.getProductDetail().setPieces(dto.getPieces());

        // Initialize wholesales if null
        if (product.getWholesales() == null) {
            product.setWholesales(new ProductWholesales());
        }

        // Update wholesales fields
        if (dto.getAmazon() != null) product.getWholesales().setAmazon(dto.getAmazon());
        if (dto.getCymax() != null) product.getWholesales().setCymax(dto.getCymax());
        if (dto.getOverstock() != null) product.getWholesales().setOverstock(dto.getOverstock());
        if (dto.getWayfair() != null) product.getWholesales().setWayfair(dto.getWayfair());
        if (dto.getEwfdirect() != null) product.getWholesales().setEwfdirect(dto.getEwfdirect());
        if (dto.getHoustondirect() != null) product.getWholesales().setHoustonDirect(dto.getHoustondirect());
        if (dto.getEwfmain() != null) product.getWholesales().setEwfmain(dto.getEwfmain());

        if (dto.getSizeShape() != null) {
            Dimension dimension = product.getDimension();
            if (dimension == null) dimension = new Dimension();
            dimension.setSizeShape(dto.getSizeShape());
            product.setDimension(dimension);
        }
    }
}

