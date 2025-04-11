package com.danny.ewf_service.utils.exports;

import com.danny.ewf_service.entity.Dimension;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.entity.product.ProductComponent;
import com.danny.ewf_service.repository.ProductComponentRepository;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.utils.CsvWriter;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
public class ShopifyExport {

    @Autowired
    private final CsvWriter csvWriter;

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final ProductComponentRepository productComponentRepository;

    public void exportShopifyProductsWeight(String filePath) throws Exception {
        List<Product> products = productRepository.findProductsByWholesalesEwfdirect();
        List<String[]> rows = new ArrayList<>();
//        String[] header = {"SKU", "QB7","shipping", "Price"};
        String[] header = {"Handle", "Title", "Variant Price"};
        rows.add(header);
        products.forEach(product -> {
            if (product.getPrice() == null) return;
            double productWeight = 0;
            double productPrice = product.getPrice().getQB7();
            double totalShipCost = 0;
            List<ProductComponent> components = product.getComponents();

            for (ProductComponent productComponent : components) {

                double shippingCost = 0;

                Dimension dimension = productComponent.getComponent().getDimension();
                long quantityBox;

                if (dimension != null) {

                    quantityBox = productComponent.getComponent().getDimension().getQuantityBox();
                    if (quantityBox == 0) {
                        quantityBox = 1;
                    }

                    double componentWeight = (dimension.getBoxLength() * dimension.getBoxWidth() * dimension.getBoxHeight()) / 139;

                    if (componentWeight < dimension.getBoxWeight()) {
                        componentWeight = dimension.getBoxWeight();
                    }
                    if (componentWeight <= 20) {
                        shippingCost = 15;
                    } else if (componentWeight <= 30) {
                        shippingCost = 20;
                    } else if (componentWeight <= 40) {
                        shippingCost = 25;
                    } else if (componentWeight <= 50) {
                        shippingCost = 30;
                    } else if (componentWeight <= 60) {
                        shippingCost = 35;
                    } else if (componentWeight <= 70) {
                        shippingCost = 47;
                    } else if (componentWeight <= 80) {
                        shippingCost = 58;
                    } else if (componentWeight <= 100) {
                        shippingCost = 65;
                    } else {
                        shippingCost = 80;
                    }

                    totalShipCost = totalShipCost + shippingCost * ((double) productComponent.getQuantity() / quantityBox);
                    productPrice = productPrice + shippingCost * ((double) productComponent.getQuantity() / quantityBox);
//                    rows.add(new String[]{"",productComponent.getComponent().getSku(), String.valueOf(shippingCost * ((double) productComponent.getQuantity()/quantityBox))});
//                    productWeight = productWeight + componentWeight * ((double) productComponent.getQuantity()/quantityBox);
                }
            }

//            rows.add(new String[]{
//                    product.getSku().toLowerCase(),
//                    String.valueOf(product.getPrice().getQB7()),
//                    String.valueOf(totalShipCost),
//                    String.valueOf(productPrice)
//            });

            if (productPrice > 2000) {
                productPrice = productPrice * 85;
            } else if (productPrice > 1000) {
                productPrice = productPrice * 90;
            } else if (productPrice > 500) {
                productPrice = productPrice * 95;
            }

            rows.add(new String[]{
                    product.getSku().toLowerCase(),
                    product.getTitle(),
                    String.valueOf(productPrice)
            });

            System.out.println("Exported " + product.getSku() + " weight " + productWeight + " price " + productPrice);
        });

        csvWriter.exportToCsv(rows, filePath);
    }

    public void exportShopifyProductsInventory(String filePath) {
        List<Object[]> rawResult = productComponentRepository.calculateListProductInventoryShopifyEWFDirectByQuantityASC();
        List<String[]> rows = new ArrayList<>();
        String[] header = {"Handle", "Title", "Option1 Name", "Option1 Value", "Option2 Name", "Option2 Value", "Option3 Name", "Option3 Value", "SKU", "HS Code", "COO", "175 Southbelt Industrial Drive"};
        rows.add(header);
        System.out.println("Found " + rawResult.size());
        for (Object[] result : rawResult) {
            if (result[0] != "" && result[1] != "" && result[2] != "") {
                rows.add(new String[]{
                        result[0].toString().toLowerCase(),
                        result[1].toString(),
                        "Title",
                        "Default Title",
                        "",
                        "",
                        "",
                        "",
                        result[0].toString(),
                        "",
                        "",
                        result[2].toString(),

                });
            }
        }
        csvWriter.exportToCsv(rows, filePath);
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
