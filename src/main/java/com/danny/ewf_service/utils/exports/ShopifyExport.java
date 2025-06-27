package com.danny.ewf_service.utils.exports;

import com.danny.ewf_service.entity.Component;
import com.danny.ewf_service.entity.Dimension;
import com.danny.ewf_service.entity.ImageUrls;
import com.danny.ewf_service.entity.Price;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.entity.product.ProductComponent;
import com.danny.ewf_service.entity.product.ProductDetail;
import com.danny.ewf_service.repository.ProductComponentRepository;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.service.CacheService;
import com.danny.ewf_service.service.ImageService;
import com.danny.ewf_service.service.InventoryService;
import com.danny.ewf_service.service.ProductService;
import com.danny.ewf_service.utils.CsvWriter;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class ShopifyExport {

    @Autowired
    private final CsvWriter csvWriter;

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final ProductComponentRepository productComponentRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private final InventoryService inventoryService;

    @Autowired
    private ImageService imageService;


    public void exportShopifyProductsPrice(String filePath) throws Exception {
        List<Product> products = productRepository.findProductsByWholesalesEwfdirect();
        List<String[]> rows = new ArrayList<>();

        String[] header = {"", "Handle", "Title", "Shipping Method", "Variant Price", "Component SKU", "Weight", "Girth", "Quantity", "Sale Price", "Shipping cost(Boston)", "Total Price", "Amazon Price", "Compare Price"};
        rows.add(header);
        try {
            products.forEach(product -> {
                if (product.getProductDetail() != null) {
                    if (product.getProductDetail().getSubCategory() != null) {
                        if (!product.getProductDetail().getSubCategory().equals("Dining Chair")) return;
                    }
                }

                if (product.getPrice() == null) return;
                double productPrice = productService.calculateEWFDirectPriceGround(product, rows);
                System.out.println("Exported " + product.getSku() + " price " + productPrice);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        csvWriter.exportToCsv(rows, filePath);
    }

    public void exportShopifyProductsInventory(String filePath) {
        try {
            List<Object[]> rawResult = productComponentRepository.calculateListProductInventoryShopifyEWFDirectByQuantityASC();
            List<String[]> rows = new ArrayList<>();
            String[] header = {"Handle", "Title", "Option1 Name", "Option1 Value", "Option2 Name", "Option2 Value", "Option3 Name", "Option3 Value", "SKU", "HS Code", "COO", "Location", "Incoming", "Unavailable", "Committed", "Available", "On hand"};
            rows.add(header);
            String title = "";
            System.out.println("Found " + rawResult.size());
            String inventory;
            for (Object[] result : rawResult) {
                if (result[0] != "" && result[1] != "" && result[2] != "") {
//                    if (result[1] == null) {
//                        Optional<Product> optionalProduct = productRepository.findBySku(result[0].toString());
//                        if (optionalProduct.isPresent()) {
//                            Product product = optionalProduct.get();
//                            if (product.getTitle() == null) {
//                                title = product.getName();
//                            } else {
//                                title = product.getTitle();
//                            }
//
//                        }
//                    }
                    Optional<Product> optionalProduct = productRepository.findBySku(result[0].toString());
                    if (optionalProduct.isPresent()) {
                        Product product = optionalProduct.get();
                        if (product.getTitle() == null) {
                            title = product.getName();
                        } else {
                            title = product.getTitle();
                        }
                        if (product.getProductDetail() != null) {
                            if (Objects.equals(product.getProductDetail().getSubCategory(), "Dining Table")) {
                                List<ProductComponent> productComponents = product.getComponents();
                                for (ProductComponent productComponent : productComponents) {
                                    Dimension dimension = productComponent.getComponent().getDimension();
                                    if (dimension != null) {
                                        System.out.println(result[0].toString() + " " + dimension.getBoxLength());
                                        if (dimension.getBoxLength() > 40) {
                                            inventory = String.valueOf(0L);
                                            rows.add(new String[]{
                                                    result[0].toString().toLowerCase(),
                                                    title,
                                                    "Title",
                                                    "Default Title",
                                                    "",
                                                    "",
                                                    "",
                                                    "",
                                                    result[0].toString(),
                                                    "",
                                                    "",
                                                    "175 Southbelt Industrial Drive",
                                                    "0",
                                                    "0",
                                                    "0",
                                                    inventory,
                                                    inventory

                                            });
                                            System.out.println(result[0].toString());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            csvWriter.exportToCsv(rows, filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void exportShopifyDiscountPrice(String filePath) {
        String skuExportListPath = "src/main/resources/data/discount_sku.csv";
        List<String> skus = csvWriter.skuListFromCsv(skuExportListPath);

        List<String> uppercaseSkus = skus.stream()
                .map(String::toUpperCase)
                .toList();

        List<String[]> rows = new ArrayList<>();
        String[] header = {"Handle", "Title", "Price / United States", "Compare At Price / United States"};
        rows.add(header);

        for (String uppercaseSku : uppercaseSkus) {
            Optional<Product> optionalProduct = productRepository.findBySku(uppercaseSku);
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                if (product.getPrice() == null) return;
                if (product.getPrice().getQB7() == 0) return;


                rows.add(new String[]{
                        product.getSku().toLowerCase(),
                        product.getLocalTitle(),
                        String.valueOf(product.getPrice().getQB7() * 0.7),
                        String.valueOf(product.getPrice().getQB7())
                });

                System.out.println("Exported " + product.getSku() + " price " + product.getPrice().getQB7() * 0.7 + "/" + product.getPrice().getQB7());
            }
        }

        csvWriter.exportToCsv(rows, filePath);
    }

    public void exportAmazonReviews() {
        List<String[]> rows = new ArrayList<>();
        String[] header = {"product_title", "product_handle", "URL_of_product_on_Amazon"};
        List<Product> products = productRepository.findProductsByWholesalesEwfdirect();
        rows.add(header);
        for (Product product : products) {
            if (product.getTitle() != null && product.getAsin() != null) {
                rows.add(new String[]{
                        product.getTitle(),
                        product.getSku().toLowerCase(),
                        "https://www.amazon.com/dp/" + product.getAsin()
                });
            }
        }
        csvWriter.exportToCsv(rows, "amazon_reviews.csv");
    }

    public void exportProductListing(List<String> skus, String filePath, boolean isImageExport) {

        String[] header = {
                "Handle",
                "Title",
                "Body (HTML)",
                "Vendor",
                "Product Category",
                "Type",
                "Tags",
                "Published",
                "Option1 Name",
                "Option1 Value",
                "Variant SKU",
                "Variant Grams",
                "Variant Inventory Tracker",
                "Variant Inventory Qty",
                "Variant Inventory Policy",
                "Variant Fulfillment Service",
                "Variant Price",
                "Variant Compare At Price",
                "Variant Requires Shipping",
                "Variant Taxable",
                "Variant Barcode",
                "Image Src",
                "Image Position",
                "Gift Card",
                "SEO Title",
                "SEO Description",
                "Google Shopping / Google Product Category",
                "Google Shopping / Gender",
                "Google Shopping / Age Group",
                "Google Shopping / MPN",
                "Finish (product.metafields.my_fields.finish)",
                "Variant Weight Unit",
                "Included / United States",
                "Status",
        };

        List<String[]> rows = new ArrayList<>();
        rows.add(header);

        List<String[]> skuRows = new ArrayList<>();


        List<Product> products;
        if (skus.isEmpty()) products = productRepository.findProductsByWholesalesEwfdirect();
        else {
            products = productRepository.findAllBySkus(skus);
        }

        double productPrice;
        double comparePrice = 0;

        List<String> images = new ArrayList<>();
        ImageUrls productImages;
        StringBuilder tags;
        String[] row;
        int newArrivalsStartIndex = products.size() - 1500;
        int index = 0;
        try {
            for (Product product : products) {
                index++;
                System.out.println("Processing " + product.getSku());
                if (product.getWholesales() != null) {
                    if (!product.getWholesales().getEwfdirect()) continue;
                }

                if (product.getDiscontinued() == null || product.getDiscontinued()) continue;
                if (product.getComponents().isEmpty()) continue;
                if (product.getTitle() == null) continue;
                if (product.getProductDetail() != null) {
                    if (product.getProductDetail().getDescription() == null) continue;
                    if (product.getProductDetail().getSubCategory() == null) continue;
//                    if (!product.getProductDetail().getSubCategory().equals("Dining Chair")) continue;
                } else {
                    continue;
                }
                if (product.getUpc() == null) continue;

                List<Product> mergedProducts = productService.findMergedProducts(product);

                if (isImageExport) {
                    productImages = imageService.parseImageJson(product.getImages());
                    images = new ArrayList<>(imageService.toList(productImages));
                    if (images.isEmpty()) continue;

                    if (mergedProducts != null) {
                        for (Product mergedProduct : mergedProducts) {
                            if (!Objects.equals(mergedProduct.getSku(), product.getSku())) {
                                productImages = imageService.parseImageJson(mergedProduct.getImages());
                                images.addAll(imageService.toList(productImages));
                            }
                        }
                    }

                    List<String> subSingleSkus = productComponentRepository.findSingleProductsByProductSku(product.getId());
                    Product subProduct;
                    for (String subSingleSku : subSingleSkus) {
                        if (subSingleSku.equals(product.getSku())) continue;
                        Optional<Product> subProductOptional = productRepository.findProductBySku(subSingleSku);
                        if (subProductOptional.isPresent()) {
                            subProduct = subProductOptional.get();
                            productImages = imageService.parseImageJson(subProduct.getImages());
                            images.addAll(imageService.toList(productImages));
                        }
                    }
                }

                ProductDetail productDetail = product.getProductDetail();
                if (productDetail == null) productDetail = new ProductDetail();
                productPrice = productService.calculateEWFDirectPriceGround(product, new ArrayList<>());

//            if (product.getPrice() != null) {
//                if (product.getPrice().getAmazonPrice() != 0) {
//                    if (productPrice / product.getPrice().getAmazonPrice() < 0.9) {
//                        comparePrice = product.getPrice().getAmazonPrice() * 1.1;
//                    }
//                }
//            }

                tags = new StringBuilder();
                if (productDetail.getSizeShape() != null) tags.append(productDetail.getSizeShape()).append(",");
                if (productDetail.getFinish() != null) tags.append(productDetail.getFinish()).append(",");
                if (productDetail.getMainCategory() != null) tags.append(productDetail.getMainCategory()).append(",");
                if (productDetail.getSubCategory() != null) tags.append(productDetail.getSubCategory()).append(",");
                if (productDetail.getCollection() != null) tags.append(productDetail.getCollection()).append(",");
                if (productDetail.getStyle() != null) tags.append(productDetail.getStyle()).append(",");
                if (productDetail.getPieces() != null) tags.append(productDetail.getPieces()).append(",");
                if (productDetail.getChairType() != null) tags.append(productDetail.getChairType()).append(",");
//                if (index > newArrivalsStartIndex) tags.append("New Arrivals,");

                Price price = product.getPrice();
                if (price != null) {
                    if (price.getPromotion() > 0) {
                        tags.append("Clearance,");
                    }
                    if (price.getAmazonPrice() != null) {
                        if (productPrice < price.getAmazonPrice()) {
                            comparePrice = price.getAmazonPrice() * 1.1;
                        }
                    }

                    if (price.getPromotion() > 0) {
                        comparePrice = productPrice * (1 + (double) (price.getPromotion() - 10) / 100);
                    }
                }


                row = new String[]{
                        product.getSku().toLowerCase(),
                        product.getTitle() != null ? product.getTitle() : product.getName(),
                        productDetail.getHtmlDescription() != null ? productDetail.getHtmlDescription() : "",
                        "East West Furniture",
                        Objects.equals(productDetail.getSubCategory(), "Dining Room Set") ? "Furniture > Furniture Sets > Kitchen & Dining Furniture Sets" : "",
                        productDetail.getSubCategory(),
                        commaRemoval(tags.toString()),
                        "TRUE",
                        "Title",
                        "Default Title",
                        product.getSku(),
                        Objects.equals(product.getShippingMethod(), "LTL") ? "22679.6185" : "0.00",
                        "shopify",
                        inventoryService.getInventoryProductCountById(product.getId()) + "",
                        "deny",
                        "manual",
                        productPrice + "",                                              // Variant Price
                        comparePrice != 0 ? comparePrice + "" : "",                     // Variant Compare At Price
                        "TRUE",
                        "TRUE",
                        product.getUpc(),
                        images.get(0).replace("\"", ""),
                        "1",                                                            // img position
                        "FALSE",
                        product.getTitle() != null ? product.getTitle() : product.getName(),
                        productDetail.getDescription() != null ? productDetail.getDescription() : "",
                        "6347",
                        "Unisex",
                        "Adult",
                        product.getSku(),
                        productDetail.getFinish() != null ? productDetail.getFinish() : "",
                        "lb",
                        "TRUE",
                        "active"
                };


                rows.add(row);
                skuRows.add(new String[]{product.getSku()});
                for (int i = 1; i < images.size(); i++) {
                    rows.add(new String[]{
                            product.getSku().toLowerCase(),
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            images.get(i).replace("\"", ""),
                            String.valueOf(i + 1),
                    });
                }

                System.out.println("Exported " + product.getSku() + " | " + images);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        csvWriter.exportToCsv(rows, filePath);
    }

    public void exportProductCustomfields(List<String> skus, String filePath) {

        String[] header = {
                "Handle",
                "Title",
                "SKU",
                "Vendor",
                "Type",
                "Published",
                "google[\"google_product_type\"]:string",
                "google[\"gender\"]:string",
                "google[\"age_group\"]:string",
                "global[\"MPN\"]:string",
                "google[\"adwords_labels\"]:string",
                "custom_fields[\"sku_table\"]:string",
                "custom_fields[\"qty\"]:string",
                "custom_fields[\"type_table\"]:string",
                "custom_fields[\"dimension\"]:string",
                "custom_fields[\"box_qty\"]:string",
                "custom_fields[\"box_dimension\"]:string",
                "custom_fields[\"group_handle\"]:string",
                "custom_fields[\"style\"]:string",
                "custom_fields[\"finish\"]:string",
                "custom_fields[\"collection\"]:string",
                "custom_fields[\"shipping_method\"]:string",
                "custom_fields[\"related_product\"]:string",
                "custom_fields[\"chair_types\"]:string",
                "custom_fields[\"local_link\"]:string",
                "custom_fields[\"amazon_link\"]:string",
                "custom_fields[\"gg_link\"]:multi_line_text_field",
                "custom_fields[\"sku_link\"]:multi_line_text_field",
                "custom_fields[\"gg_link_title\"]:single_line_text_field",
                "mm-google-shopping[\"gender\"]:string",
                "mm-google-shopping[\"age_group\"]:string",
                "global[\"description_tag\"]:string",
                "custom_fields[\"shape\"]:string",
                "custom_fields[\"pieces\"]:string"
        };

        List<String[]> rows = new ArrayList<>();
        rows.add(header);

        List<Product> products;
        if (skus.isEmpty()) products = productRepository.findProductsByWholesalesEwfdirect();
        else {
            products = productRepository.findAllBySkus(skus);
        }

        List<String> skuList = products.stream()
                .map(Product::getSku)               // Extract the `sku` of each product
                .filter(Objects::nonNull)           // Filter out null `sku` values
                .toList();


        String title;
        String[] row;
        List<String[]> skuTable;
        int boxQty = 0;

        for (Product product : products) {
            System.out.println("Processing " + product.getSku());
//            if (!Objects.equals(product.getSku(), "CNDA5-07-T12")) continue;
            if (product.getDiscontinued() == null || product.getDiscontinued()) continue;
            if (product.getComponents().isEmpty()) continue;
            if (product.getProductDetail() == null) continue;

            if (product.getTitle() != null) {
                title = product.getTitle();
            } else {
                if (product.getName() != null) {
                    title = product.getName();
                } else {
                    continue;
                }
            }
            ProductDetail productDetail = product.getProductDetail();

            skuTable = new ArrayList<>();

            List<Product> mergedProducts = productService.findMergedProducts(product);

            if (mergedProducts != null) {
                for (Product mergedProduct : mergedProducts) {
                    String lwh = "";
                    if (mergedProduct.getDimension() != null) {
                        if (mergedProduct.getDimension().getLwh() != null) {
                            lwh = mergedProduct.getDimension().getLwh();
                        }
                    }
                    for (ProductComponent productComponent : mergedProduct.getComponents()) {
                        if (productComponent.getComponent().getSubType() != null) {
                            if (productComponent.getComponent().getSubType().equals("Dining Table Top") || productComponent.getComponent().getSubType().equals("Dining Table")) {
                                if (productComponent.getComponent().getDimension().getLwh() != null) {
                                    lwh = productComponent.getComponent().getDimension().getLwh();
                                    break;
                                }
                            }
                        }
                    }

                    boxQty = mergedProduct.getComponents().size();
                    skuTable.add(new String[]{
                            mergedProduct.getSku(),
                            1 + "",
                            mergedProduct.getProductDetail().getSubCategory() != null ? mergedProduct.getProductDetail().getSubCategory() : "",
                            lwh,
                            boxQty == 0 ? "" : boxQty + "",
                            ""
                    });

                }
                skuTable.forEach(rowEntry -> System.out.println(Arrays.toString(rowEntry)));
            }

            for (ProductComponent productComponent : product.getComponents()) {
                Component component = productComponent.getComponent();
                if (Objects.equals(component.getType(), "Single")) {
                    Dimension dimension = component.getDimension();
                    if (dimension == null) dimension = new Dimension();
                    String lwh = "";
                    if (dimension.getLength() != null) {
                        lwh = lwhToString(dimension.getLength(), dimension.getWidth(), dimension.getHeight());
                    }
                    if (component.getSubType() != null) {
                        if (component.getSubType().equals("Dining Table Top") || component.getSubType().equals("Dining Table")) {
                            if (dimension.getLwh() != null) lwh = dimension.getLwh();
                        }
                    }
                    skuTable.add(new String[]{
                            component.getSku(),
                            productComponent.getQuantity() + "",
                            component.getSubType() != null ? component.getSubType() : "",
                            lwh,
                            dimension.getQuantityBox() != null ? productComponent.getQuantity() / dimension.getQuantityBox() + "" : "",
                            dimension.getBoxLength() != null
                                    ? lwhToString(dimension.getBoxLength(), dimension.getBoxWidth(), dimension.getBoxHeight()) : "",
                    });
                }
            }

            List<String> subSingleSkus = productComponentRepository.findSingleProductsByProductSku(product.getId());
            List<Product> subSingleProducts = productRepository.findAllBySkus(subSingleSkus);

            if (skuTable.isEmpty()) {
                for (ProductComponent productComponent : product.getComponents()) {
                    Component component = productComponent.getComponent();
                    Dimension dimension = component.getDimension();
                    String lwh = "";
                    if (dimension.getLength() != null) {
                        lwh = lwhToString(dimension.getLength(), dimension.getWidth(), dimension.getHeight());
                    }
                    if (component.getSubType() != null) {
                        if (component.getSubType().equals("Dining Table Top") || component.getSubType().equals("Dining Table")) {
                            if (dimension.getLwh() != null) lwh = dimension.getLwh();
                        }
                    }

                    skuTable.add(new String[]{
                            component.getSku(),
                            productComponent.getQuantity() + "",
                            component.getSubType() != null ? component.getSubType() : "",
                            lwh,
                            dimension.getQuantityBox() != null ? productComponent.getQuantity() / dimension.getQuantityBox() + "" : "",
                            dimension.getBoxLength() != null ? lwhToString(dimension.getBoxLength(), dimension.getBoxWidth(), dimension.getBoxHeight()) : ""
                    });
                }
            }

            row = new String[]{
                    product.getSku().toLowerCase(),
                    title,
                    product.getSku().toUpperCase(),
                    "East West Furniture",
                    productDetail.getSubCategory() != null ? productDetail.getSubCategory() : "",
                    "TRUE",
                    productDetail.getMainCategory() != null ? productDetail.getMainCategory() + " > " + productDetail.getSubCategory() : "",
                    "Unisex",
                    "Adult",
                    product.getSku().toUpperCase(),
                    productDetail.getSubCategory() != null ? productDetail.getSubCategory() : "",
                    getTableValueFromIndex(0, skuTable),
                    getTableValueFromIndex(1, skuTable),
                    getTableValueFromIndex(2, skuTable),
                    getTableValueFromIndex(3, skuTable),
                    getTableValueFromIndex(4, skuTable),
                    getTableValueFromIndex(5, skuTable),
                    getGroupHandle(skuList, product.getSku()),
                    productDetail.getStyle() != null ? productDetail.getStyle() : "",
                    productDetail.getFinish() != null ? productDetail.getFinish() : "",
                    productDetail.getCollection() != null ? productDetail.getCollection() : "",
                    product.getShippingMethod() != null ? product.getShippingMethod() : "",
                    getRelatedProduct(product, skuList, mergedProducts, subSingleProducts),
                    productDetail.getChairType() != null ? productDetail.getChairType() : "",
                    "https://ewfdirect.com/products/" + product.getSku().toLowerCase(),
                    "http://www.amazon.com/dp/" + product.getAsin() + "/ref=nosim?tag=eastwest00-20&th=1",
                    "https://www.google.com/search?q=" + product.getSku() + "&source=lnms&tbm=shop&sa=",
                    "/products/" + product.getSku(),
                    product.getSku(),
                    "Unisex",
                    "Adult",
                    productDetail.getDescription() != null ? productDetail.getDescription() : "",
                    productDetail.getSizeShape() != null ? productDetail.getSizeShape() : "",
                    productDetail.getPieces() != null ? productDetail.getPieces() : ""
            };
            rows.add(row);
        }

        csvWriter.exportToCsv(rows, filePath);
    }

    private String getTableValueFromIndex(int index, List<String[]> rows) {
        StringBuilder tableValue = new StringBuilder();
        for (String[] entry : rows) {
            if (entry.length > 0 && entry[index] != null) {
                tableValue.append(",").append(entry[index]);
            }
        }

        return commaRemoval(tableValue.toString());
    }

    private String getGroupHandle(List<String> skus, String sku) {
        if (sku == null || sku.length() < 4) {
            return ""; // Handle null or invalid input
        }
        String prefix = sku.contains("-") ? sku.substring(0, sku.indexOf("-")) : sku.substring(0, 3);
        return skus.stream()
                .filter(s -> s != null && s.startsWith(prefix)) // Match SKUs starting with the prefix
                .distinct()                                    // Remove duplicates
                .collect(java.util.stream.Collectors.joining(","));
    }

    private String getRelatedProduct(Product product, List<String> skuList, List<Product> mergedProducts, List<Product> subSingleProducts) {
        StringBuilder relatedProductSku = new StringBuilder();
        if (product.getComponents() == null) return "";
        if (mergedProducts != null) {
            for (Product mergedProduct : mergedProducts) {
                relatedProductSku.append(",").append(mergedProduct.getSku());
            }
            relatedProductSku.append(",").append(getGroupHandle(skuList, product.getSku()));
        }

        if (subSingleProducts != null) {
            for (Product subSingleProduct : subSingleProducts) {
                relatedProductSku.append(",").append(subSingleProduct.getSku());
            }
            relatedProductSku.append(",").append(getGroupHandle(skuList, product.getSku()));
        }

        return commaRemoval(relatedProductSku.toString());
    }

    private String commaRemoval(String text) {
        if (text == null || text.isEmpty()) {
            return ""; // Handle null or empty input
        }

        text = text.replaceAll("^,|,$", "");

        // Replace double commas (",,") with a single comma (",")
        text = text.replaceAll(",{2,}", ",");

        return text;

    }

    private String lwhToString(double length, double width, double height) {
        if (Double.isNaN(length) || Double.isNaN(width) || Double.isNaN(height)) {
            return "";
        }

        return "L " + (int) Math.floor(length) + " x W " + (int) Math.floor(width) + " x H " + (int) Math.floor(height);
    }
}
