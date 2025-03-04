package com.danny.ewf_service.utils.imports;

import com.danny.ewf_service.entity.ImageUrls;
import com.danny.ewf_service.entity.Product;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.utils.ImageCheck;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

@Service
@AllArgsConstructor
public class ImagesImport {


    @Autowired
    private ImageCheck imageCheck;

    @Autowired
    private final ProductRepository productRepository;


    public void updateProductImages() {
        String filepath = "src/main/resources/data/sirv.csv";
        List<Product> products = productRepository.findAllProducts();
        for (Product product : products) {
            ImageUrls imageUrls = new ImageUrls();
            try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] columns = line.split(",");

                    if (columns.length >= 2) {
                        if (columns[1].contains(product.getSku()) && columns[1].contains(".jpg")) {
                            if (columns[1].contains("/DIM/")) {
                                imageUrls.getDim().add(columns[1]);
                            } else {
                                imageUrls.getImg().add(columns[1]);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading the CSV file: " + e.getMessage());
            }
            String imagesJsonString = imageUrls.buildJsonString();
            product.setImages(imagesJsonString);
            productRepository.save(product);
            System.out.println("SAVED : " + product.getSku() + "/n IMG : " + imagesJsonString);
        }
    }
}