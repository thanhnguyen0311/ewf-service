package com.danny.ewf_service.utils.exports;

import com.danny.ewf_service.entity.Product;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.utils.ImageCheck;
import com.danny.ewf_service.utils.ImageProcessor;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ImagesExport {

    @Autowired
    private final ImageCheck imageCheck;

    @Autowired
    private final CsvWriter csvWriter;

    @Autowired
    private final ImageProcessor imageProcessor;
    
    @Autowired
    private final ProductRepository productRepository;


    public void exportImagesShopifyByCategory(String category) {
        List<Product> products = productRepository.findByCategory(category);
        String[] header = {"Handle", "Title", "Image Src"};

        List<String[]> rows = new ArrayList<>();
        ImageProcessor.ImageUrls imageUrls = new ImageProcessor.ImageUrls();
        rows.add(header);
        int count;
        String[] row;
        for (Product product : products) {
            count = 0;
            imageUrls = imageProcessor.parseImageJson(product.getImages());
            for (String imgLink : imageUrls.getImg()) {
                if (imageCheck.isImageLinkAlive(imgLink)) {
                    if (count ==0) {
                        row = new String[]{product.getSku().toLowerCase(), product.getLocalProduct().getLocalTitle(), imgLink};
                    } else {
                        row = new String[]{product.getSku().toLowerCase(), "", imgLink};
                    }
                    rows.add(row);
                    count++;
                }
            }
            for (String dim : imageUrls.getDim()) {
                row = new String[]{product.getSku().toLowerCase(), "", dim};
                rows.add(row);
            }
        }
        csvWriter.exportToCsv(rows, "floor_images.csv");
    }
}
