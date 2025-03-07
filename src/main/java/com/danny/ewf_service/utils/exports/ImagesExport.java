package com.danny.ewf_service.utils.exports;

import com.danny.ewf_service.entity.ImageUrls;
import com.danny.ewf_service.entity.Product;
import com.danny.ewf_service.entity.ProductComponent;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.service.ProductService;
import com.danny.ewf_service.utils.ImageCheck;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@AllArgsConstructor
public class ImagesExport {

    @Autowired
    private final ImageCheck imageCheck;

    @Autowired
    private final CsvWriter csvWriter;

    private final ObjectMapper mapper;

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final ProductService productService;


    public void exportImagesShopifyMain(String filePath) throws JsonProcessingException {
        List<Product> products = productRepository.findAll();
        String[] header = {"Handle", "Title", "Image Src", "Image Position"};
        String skuExportListPath = "src/main/resources/data/CUSTOMFIELD_EWFDIRECT.csv";
        Set<String> skus = csvWriter.skuListFromCsv(skuExportListPath);
        List<String[]> rows = new ArrayList<>();
        ImageUrls productImages;
        ImageUrls componentImages;
        rows.add(header);
        int imgPos;
        int count = 0;
        for (Product product : products) {
            if (product.getLocalProduct().getLocalTitle() == null || product.getLocalProduct().getLocalTitle().isEmpty()) continue;
            if (!skus.contains(product.getSku().toLowerCase())) continue;
            imgPos = 1;
            productImages = mapper.readValue(product.getImages(), ImageUrls.class);

            imgPos = addImagesToRows(product.getSku().toLowerCase(), product.getLocalProduct().getLocalTitle(), productImages, rows, imgPos);


            List<ProductComponent> components = product.getProductComponents();
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