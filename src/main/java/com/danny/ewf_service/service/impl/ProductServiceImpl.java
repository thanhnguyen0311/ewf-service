package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.converter.IProductMapper;
import com.danny.ewf_service.entity.Component;
import com.danny.ewf_service.entity.Dimension;
import com.danny.ewf_service.entity.Price;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.entity.product.ProductDetail;
import com.danny.ewf_service.entity.product.ProductWholesales;
import com.danny.ewf_service.payload.request.product.ProductComponentRequestDto;
import com.danny.ewf_service.payload.request.product.ProductDetailRequestDto;
import com.danny.ewf_service.entity.product.ProductComponent;
import com.danny.ewf_service.payload.response.ComponentProductDetailResponseDto;
import com.danny.ewf_service.payload.response.product.ProductDetailResponseDto;
import com.danny.ewf_service.payload.response.product.ProductResponseDto;
import com.danny.ewf_service.payload.response.product.ProductSearchResponseDto;
import com.danny.ewf_service.repository.ProductComponentRepository;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.service.*;
import com.danny.ewf_service.utils.imports.SKUGenerator;
import jakarta.annotation.PostConstruct;
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
    @Autowired
    private ImageService imageService;

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
        // Process products in parallel
        List<ProductDetailResponseDto> productDetailResponseDtos;
        try {
            productDetailResponseDtos = products
                    .parallelStream() // Process products in parallel
                    .map(this::toProductDetailResponseDto) // Map each product to a ProductDetailResponseDto using the existing method
                    .toList();
        } catch (Exception e) {
            System.err.println("Error while mapping all products: " + e.getMessage());
            throw new RuntimeException("Failed to fetch products from the database", e);
        }
        System.out.println(productDetailResponseDtos.size());
        return productDetailResponseDtos;
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
        return toProductDetailResponseDto(savedProduct);
    }

    @Override
    public List<String> getAllImagesProduct(Product product) {
        return List.of();
    }

    @Override
    public double calculateEWFDirectPriceGround(Product product, List<String[]> rows) {
        System.out.println("Processing " + product.getSku());
        double productWeight = 0;
        double productPrice;
        double totalShipCost = 0;
        double totalQB1 = 0;
        int stt = 1;
        long totalQuantity = 0;
        double comparePrice = 0;
        List<ProductComponent> components = product.getComponents();

        for (ProductComponent productComponent : components) {
            double shippingCost;
            double girth;
            Dimension dimension = productComponent.getComponent().getDimension();
            long quantityBox;
            double componentPrice = productComponent.getComponent().getPrice().getQB3();
            double boxCount;
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

                if  (productComponent.getComponent().getSku().contains("DSL-")) shippingCost = 0;


                girth = dimension.getBoxLength() + 2 * (dimension.getBoxWidth() + dimension.getBoxHeight());

                if (girth > 160) {
                    shippingCost = shippingCost + 50;
                } else if (girth > 136) {
                    shippingCost = shippingCost + 30;
                } else if (girth > 118) {
                    shippingCost = shippingCost + 20;
                }

                if (components.size() == 1) {
                    if (girth > 118) {
                        shippingCost = shippingCost + 35;
                    }
                    if (girth > 160) {
                        shippingCost = shippingCost + 35;
                    }
                    if (dimension.getBoxLength() >= 44) {
                        shippingCost = shippingCost + 35;
                    }
                }


                totalQB1 = totalQB1 + (componentPrice * productComponent.getQuantity());
                shippingCost = shippingCost * boxCount;
                totalShipCost = totalShipCost + shippingCost;
                totalQuantity = totalQuantity + productComponent.getQuantity();

                rows.add(new String[]{
                        String.valueOf(stt),
                        product.getSku(),
                        "",
                        product.getShippingMethod(),
                        "",
                        productComponent.getComponent().getSku(),
                        String.valueOf(componentWeight),
                        String.valueOf(girth),
                        String.valueOf(productComponent.getQuantity()),
                        String.valueOf(productComponent.getComponent().getPrice().getQB3()),
                        String.valueOf(shippingCost),
                        String.valueOf(componentPrice * productComponent.getQuantity() + shippingCost),
                });
                stt++;
            }
        }

        Price price = product.getPrice();
        if (price == null) price = new Price();
        if (price.getPromotion() != null) {
            if (price.getPromotion() > 0) {
                comparePrice = totalQB1 + totalShipCost;
                totalQB1 = totalQB1 * (1 - (double) price.getPromotion() / 100);
            }
        }



        productPrice = totalQB1 + totalShipCost;
        productPrice = productPrice * 1.03;

        if (product.getPrice() == null) product.setPrice(new Price());
        product.getPrice().setEwfdirect(productPrice);
        productRepository.save(product);

        if (product.getPrice() != null) {
            if (product.getPrice().getAmazonPrice() != null) {
                if (productPrice < product.getPrice().getAmazonPrice() && comparePrice == 0) {
                    comparePrice = product.getPrice().getAmazonPrice() * 1.1;
                }
            }
        }
        rows.add(new String[]{
                String.valueOf(stt),
                product.getSku(),
                product.getTitle(),
                String.valueOf(product.getShippingMethod()),
                String.valueOf(productPrice),
                "", "", "",
                String.valueOf(totalQuantity),
                String.valueOf(totalQB1),
                String.valueOf(totalShipCost),
                String.valueOf(productPrice),
                price.getAmazonPrice() != null ? String.valueOf(price.getAmazonPrice()) : "",
                String.valueOf(comparePrice),
                "http://www.amazon.com/dp/" + product.getAsin(),
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

        if (dto.getComponents() != null) {
            List<ProductComponent> components = new ArrayList<>();
            for (ProductComponentRequestDto componentDto : dto.getComponents()) {
                Component component = componentService.findComponentById(componentDto.getId());
                if (component == null) continue;
                ProductComponent productComponent = new ProductComponent();
                productComponent.setComponent(component);
                productComponent.setQuantity(componentDto.getQuantity());
                components.add(productComponent);
            }
            product.setComponents(components);
        }

        if (dto.getImages() != null) product.setImages(imageService.buildJsonString(dto.getImages()));

    }

    private ProductDetailResponseDto toProductDetailResponseDto(Product product) {
        ProductDetailResponseDto responseDto = new ProductDetailResponseDto();
        if (product != null) {
            if (product.getId() != null) responseDto.setId(product.getId());
            if (product.getSku() != null) responseDto.setSku(product.getSku());
            if (product.getLocalSku() != null) responseDto.setLocalSku(product.getLocalSku());
            if (product.getUpc() != null) responseDto.setUpc(product.getUpc());
            if (product.getOrder() != null) responseDto.setOrder(product.getOrder());
            if (product.getCategory() != null) responseDto.setCategory(product.getCategory());
            if (product.getAsin() != null) responseDto.setAsin(product.getAsin());
            if (product.getTitle() != null) responseDto.setTitle(product.getTitle());
            if (product.getLocalTitle() != null) responseDto.setLocalTitle(product.getLocalTitle());
            if (product.getShippingMethod() != null) responseDto.setShippingMethod(product.getShippingMethod());
            if (product.getType() != null) responseDto.setType(product.getType());
            if (product.getDiscontinued() != null) responseDto.setDiscontinued(product.getDiscontinued());


            // Wholesale mappings
            if (product.getWholesales() != null) {
                if (product.getWholesales().getAmazon() != null) responseDto.setAmazon(product.getWholesales().getAmazon());
                if (product.getWholesales().getCymax() != null) responseDto.setCymax(product.getWholesales().getCymax());
                if (product.getWholesales().getWayfair() != null) responseDto.setWayfair(product.getWholesales().getWayfair());
                if (product.getWholesales().getEwfdirect() != null) responseDto.setEwfdirect(product.getWholesales().getEwfdirect());
                if (product.getWholesales().getEwfmain() != null) responseDto.setEwfmain(product.getWholesales().getEwfmain());
                if (product.getWholesales().getHoustonDirect() != null) responseDto.setHoustonDirect(product.getWholesales().getHoustonDirect());
                if (product.getWholesales().getOverstock() != null) responseDto.setOverstock(product.getWholesales().getOverstock());
            }


            // Product detail mappings
            if (product.getProductDetail() != null) {
                if (product.getProductDetail().getDescription() != null)
                    responseDto.setDescription(product.getProductDetail().getDescription());
                if (product.getProductDetail().getHtmlDescription() != null)
                    responseDto.setHtmlDescription(product.getProductDetail().getHtmlDescription());
                if (product.getProductDetail().getMainCategory() != null)
                    responseDto.setMainCategory(product.getProductDetail().getMainCategory());
                if (product.getProductDetail().getSubCategory() != null)
                    responseDto.setSubCategory(product.getProductDetail().getSubCategory());
                if (product.getProductDetail().getCollection() != null)
                    responseDto.setCollection(product.getProductDetail().getCollection());
                if (product.getProductDetail().getPieces() != null)
                    responseDto.setPieces(product.getProductDetail().getPieces());
            }

            // Dimension mappings
            if (product.getDimension() != null) responseDto.setSizeShape(product.getDimension().getSizeShape());

            List<ComponentProductDetailResponseDto> componentList = new ArrayList<>();
            if (product.getComponents() != null) {
                for (ProductComponent productComponent : product.getComponents()) {
                    componentList.add(
                            new ComponentProductDetailResponseDto(
                                    productComponent.getId(),
                                    productComponent.getComponent().getId(),
                                    productComponent.getComponent().getSku(),
                                    productComponent.getQuantity(),
                                    productComponent.getComponent().getPos(),
                                    productComponent.getComponent().getDimension()
                            ));
                }
                responseDto.setComponents(componentList);
            }

            if (product.getImages() != null) {
                responseDto.setImages(imageService.parseImageJson(product.getImages()));
            }
        }

        return responseDto;
    }

}

