package com.danny.ewf_service.utils.exports;

import com.danny.ewf_service.entity.Dimension;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.entity.product.ProductComponent;
import com.danny.ewf_service.repository.ProductComponentRepository;
import com.danny.ewf_service.repository.ProductRepository;
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

    public void exportShopifyProductsPrice(String filePath) throws Exception {
        List<Product> products = productRepository.findProductsByWholesalesEwfdirect();
        List<String[]> rows = new ArrayList<>();

        String[] header = {"", "Handle", "Title", "Shipping Method" , "Variant Price","Component SKU", "Weight", "Girth", "Quantity", "Sale Price", "Shipping cost(Boston)", "Total Price", "Amazon Price"};
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
}
