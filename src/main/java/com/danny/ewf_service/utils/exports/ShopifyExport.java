package com.danny.ewf_service.utils.exports;

import com.danny.ewf_service.entity.Dimension;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.entity.product.ProductComponent;
import com.danny.ewf_service.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ShopifyExport {
    @Autowired
    private final CsvWriter csvWriter;

    @Autowired
    private final ProductRepository productRepository;

    public void exportShopifyProductsWeight(String filePath) {
        String skuExportListPath = "src/main/resources/data/report.csv";
        Set<String> skus = csvWriter.skuListFromCsv(skuExportListPath);
        List<String> uppercaseSkus = skus.stream()
                .map(String::toUpperCase)
                .toList();
        List<Product> products = productRepository.findAllBySkuInIgnoreCase(uppercaseSkus);
        System.out.println(products.size());

        Set<String> foundSkus = products.stream()
                .map(Product::getSku)
                .collect(Collectors.toSet());


        List<String> missingSkus = uppercaseSkus.stream()
                .filter(sku -> !foundSkus.contains(sku))
                .toList();


        List<String[]> rows = new ArrayList<>();

        String[] header = {"Handle", "Title", "Variant Grams", "Variant Price"};

        rows.add(header);
        products.forEach(product -> {
            double productWeight = 0;
            double productPrice = product.getPrice().getQB7();
            double shippingCost;
            List<ProductComponent> components = product.getProductComponents();
            for (ProductComponent productComponent : components) {
                Dimension dimension = productComponent.getComponent().getDimension();
                double componentWeight = (dimension.getBoxLength() * dimension.getBoxWidth() * dimension.getBoxHeight()) / 139;
                if (componentWeight < dimension.getBoxWeight()) {
                    componentWeight = dimension.getBoxWeight();
                }
                if (componentWeight <= 30) {
                    shippingCost = 25;
                } else if (componentWeight <= 40) {
                    shippingCost = 30;
                } else if (componentWeight <= 50) {
                    shippingCost = 40;
                } else if (componentWeight <= 60) {
                    shippingCost = 50;
                } else if (componentWeight <= 70) {
                    shippingCost = 60;
                } else if (componentWeight <= 80) {
                    shippingCost = 70;
                } else {
                    shippingCost = 80;
                }
                productPrice = productPrice + shippingCost;

                productWeight = productWeight + componentWeight * productComponent.getQuantity();

                if (productWeight == 0.0) {
                    productPrice = productPrice * 1.3;
                }
            }
            rows.add(new String[]{
                    product.getSku().toLowerCase(),
                    product.getTitle(),
                    String.valueOf(productWeight * 453.592),
                    String.valueOf(productPrice)
            });
            System.out.println("Exported " + product.getSku() + " weight " + productWeight + " price " + productPrice);
        });


        for (String missingSku : missingSkus) {
            rows.add(new String[]{missingSku});
        }


        csvWriter.exportToCsv(rows, filePath);
    }
}
