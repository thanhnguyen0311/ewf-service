package com.danny.ewf_service.utils.exports;

import com.danny.ewf_service.entity.Dimension;
import com.danny.ewf_service.entity.ImageUrls;
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
    private final CacheService cacheService;

    @Autowired
    private final InventoryService inventoryService;

    @Autowired
    private ImageService imageService;


    public void exportShopifyProductsPrice(String filePath) throws Exception {
        List<Product> products = productRepository.findProductsByWholesalesEwfdirect();
        List<String[]> rows = new ArrayList<>();

        String[] header = {"", "Handle", "Title", "Shipping Method", "Variant Price", "Component SKU", "Weight", "Girth", "Quantity", "Sale Price", "Shipping cost(Boston)", "Total Price", "Amazon Price"};
        rows.add(header);
        products.forEach(product -> {
            if (product.getPrice() == null) return;
            double productPrice = productService.calculateEWFDirectPriceGround(product, rows);
            System.out.println("Exported " + product.getSku() + " price " + productPrice);
        });

        csvWriter.exportToCsv(rows, filePath);
    }

    public void exportShopifyProductsInventory(String filePath) {
        try {
            List<Object[]> rawResult = productComponentRepository.calculateListProductInventoryShopifyEWFDirectByQuantityASC();
            List<String[]> rows = new ArrayList<>();
            String[] header = {"Handle", "Title", "Option1 Name", "Option1 Value", "Option2 Name", "Option2 Value", "Option3 Name", "Option3 Value", "SKU", "HS Code", "COO", "Location", "Incoming", "Unavailable","Committed","Available","On hand"};
            rows.add(header);
            String title = "";
            System.out.println("Found " + rawResult.size());
            for (Object[] result : rawResult) {
                if (result[0] != "" && result[1] != "" && result[2] != "") {
                    if (result[1] == null) {
                        Optional<Product> optionalProduct = productRepository.findBySku(result[0].toString());
                        if (optionalProduct.isPresent()) {
                            Product product = optionalProduct.get();
                            if (product.getTitle() == null) {
                                title = product.getName();
                            } else {
                                title = product.getTitle();
                            }
                        }
                    }
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
                            result[2].toString(),
                            result[2].toString()

                    });
                    System.out.println(result[0].toString());
                }
            }
            csvWriter.exportToCsv(rows, filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exportShopifyProductsTrackInventory(String filePath) {
        List<Object[]> rawResult = productComponentRepository.calculateListProductInventoryShopifyEWFDirectByQuantityASC();
        List<String[]> rows = new ArrayList<>();
        String[] header = {"Handle", "Title", "Variant Inventory Policy"};
        rows.add(header);
        for (Object[] result : rawResult) {
            if (result[0] != "" && result[1] != "") {
                rows.add(new String[]{
                        result[0].toString().toLowerCase(),
                        result[1].toString(),
                        "deny"
                });
            }
        }
        csvWriter.exportToCsv(rows, filePath);
    }

    public void exportShopifyDiscountPrice(String filePath) {
        String skuExportListPath = "src/main/resources/data/discount_sku.csv";
        Set<String> skus = csvWriter.skuListFromCsv(skuExportListPath);

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

    public void exportProductListing(List<Long> ids, String filePath) {
        List<Product> products;
        if (ids.isEmpty()) products = productRepository.findAllProducts();
        else {
            products = productRepository.findAllByIds(ids);
        }
        List<String[]> rows = new ArrayList<>();
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
                "Compare At Price / United States",
                "Status",
        };

        rows.add(header);

        double productPrice;
        double comparePrice = 0;

        List<String>  images;
        ImageUrls productImages;

        for (Product product : products) {

            productImages = imageService.parseImageJson(product.getImages());
            images = new ArrayList<>(imageService.toList(productImages));

            if (images.isEmpty()) continue;

            if (product.getComponents() == null)  continue;

            List<Product> mergedProducts = productService.findMergedProducts(product);

            if (mergedProducts != null) {
                for (Product mergedProduct : mergedProducts) {
                    if (!Objects.equals(mergedProduct.getSku(), product.getSku())) {

                        productImages = imageService.parseImageJson(mergedProduct.getImages());
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

            rows.add(new String[]{
                    product.getSku().toLowerCase(),
                    product.getTitle() != null ? product.getTitle() : product.getName(),
                    productDetail.getHtmlDescription() != null ? productDetail.getHtmlDescription() : "",
                    "East West Furniture",
                    Objects.equals(productDetail.getSubCategory(), "Dining Room Set") ? "Furniture > Furniture Sets > Kitchen & Dining Furniture Sets" : "",
                    productDetail.getSubCategory(),
                    productDetail.getSizeShape() != null ? productDetail.getSizeShape() : "",
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
                    images.get(0),                                                  // img src
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
                    "",
                    "active"
            });
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
                        images.get(i),
                        String.valueOf(i + 1),
                });
            }
            System.out.println("Exported " + product.getSku() + " | " + images);
        }
        csvWriter.exportToCsv(rows, filePath);
    }
}
