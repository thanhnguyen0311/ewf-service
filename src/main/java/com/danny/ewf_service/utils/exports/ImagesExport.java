package com.danny.ewf_service.utils.exports;

import com.danny.ewf_service.entity.Component;
import com.danny.ewf_service.entity.ImageUrls;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.entity.product.ProductComponent;
import com.danny.ewf_service.repository.ComponentRepository;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class ImagesExport {


    @Autowired
    private final CsvWriter csvWriter;

    private final ObjectMapper mapper;

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final ProductService productService;

    @Autowired
    private final ComponentRepository componentRepository;


    public void updateImagesShopifyFromList(String filePath) throws JsonProcessingException {
        String[] header = {"Handle", "Title", "Image Src", "Image Position"};
        String skuExportListPath = "src/main/resources/data/report.csv";
        Map<String, String> skuTitleMapping = csvWriter.skuTitleListFromCsv(skuExportListPath);
        List<String[]> rows = new ArrayList<>();
        ImageUrls productImages;
        ImageUrls componentImages;
        rows.add(header);
        int imgPos;
        int count = 0;

        for (Map.Entry<String, String> entry : skuTitleMapping.entrySet()) {
            Product product = productRepository.findBySku(entry.getKey()).orElse(null);
            if (product != null) {
                imgPos = 1;
                productImages = mapper.readValue(product.getImages(), ImageUrls.class);
                imgPos = addImagesToRows(entry.getKey(), entry.getValue(), productImages, rows, imgPos);

                List<ProductComponent> components = product.getComponents();

                for (ProductComponent productComponent : components) {
                    if (Objects.equals(productComponent.getComponent().getType(), "Single")) {
                        componentImages = mapper.readValue(productComponent.getComponent().getImages(), ImageUrls.class);
                        imgPos = addImagesToRows(entry.getKey(), "", componentImages, rows, imgPos);
                    }
                }

                List<Product> mergedProducts = productService.findMergedProducts(product);

                if (mergedProducts != null) {
                    for (Product mergedProduct : mergedProducts) {
                        productImages = mapper.readValue(mergedProduct.getImages(), ImageUrls.class);
                        imgPos = addImagesToRows(entry.getKey(), "", productImages, rows, imgPos);
                        System.out.println("Found " + mergedProduct.getSku() + " for " + product.getSku());
                    }
                }
            } else {
                Component component = componentRepository.findBySku(entry.getKey().toUpperCase()).orElse(null);
                if (component != null) {
                    imgPos = 1;
                    componentImages = mapper.readValue(component.getImages(), ImageUrls.class);
                    addImagesToRows(entry.getKey(), entry.getValue(), componentImages, rows, imgPos);
                }
            }
            count++;
        }

        System.out.println(count + " products exported");
        csvWriter.exportToCsv(rows, filePath);
    }

    public void exportImagesShopifyMain(String filePath) throws JsonProcessingException {
        List<Product> products = productRepository.findAll();
        String[] header = {"Handle", "Title", "Image Src", "Image Position"};
        String skuExportListPath = "src/main/resources/data/report.csv";
        Set<String> skus = csvWriter.skuListFromCsv(skuExportListPath);
        List<String[]> rows = new ArrayList<>();
        ImageUrls productImages;
        ImageUrls componentImages;
        rows.add(header);

        int imgPos;
        int count = 0;

        for (Product product : products) {
            if (product.getLocalTitle() == null || product.getLocalTitle().isEmpty()) continue;
            if (!skus.contains(product.getSku().toLowerCase())) continue;
            imgPos = 1;
            productImages = mapper.readValue(product.getImages(), ImageUrls.class);

            imgPos = addImagesToRows(product.getSku().toLowerCase(), product.getLocalTitle(), productImages, rows, imgPos);

            List<ProductComponent> components = product.getComponents();
            for (ProductComponent productComponent : components) {
                if (Objects.equals(productComponent.getComponent().getType(), "Single")) {
                    componentImages = mapper.readValue(productComponent.getComponent().getImages(), ImageUrls.class);
                    imgPos = addImagesToRows(product.getSku().toLowerCase(), "", componentImages, rows, imgPos);
                }
            }

            List<Product> mergedProducts = productService.findMergedProducts(product);
            if (mergedProducts != null) {
                for (Product mergedProduct : mergedProducts) {
                    productImages = mapper.readValue(mergedProduct.getImages(), ImageUrls.class);
                    imgPos = addImagesToRows(product.getSku().toLowerCase(), "", productImages, rows, imgPos);
                    System.out.println("Found " + mergedProduct.getSku() + " for " + product.getSku());
                }
            }
            count++;
        }
        System.out.println(count + " products exported");
        csvWriter.exportToCsv(rows, filePath);
    }


    private int addImagesToRows(String sku, String title, ImageUrls imageUrls, List<String[]> rows, int imgPos) {
        for (String imgLink : imageUrls.getImg()) {
            rows.add(new String[]{sku, (imgPos == 1 ? title : ""), imgLink, String.valueOf(imgPos)});
            imgPos++;
        }
        for (String dim : imageUrls.getDim()) {
            rows.add(new String[]{sku, "", dim, String.valueOf(imgPos)});
            imgPos++;
        }
        return imgPos;
    }
}