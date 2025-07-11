package com.danny.ewf_service.utils.imports;

import com.danny.ewf_service.entity.Component;
import com.danny.ewf_service.entity.ImageUrls;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.repository.ComponentRepository;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.service.ImageService;
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

    @Autowired
    private final ComponentRepository componentRepository;

    private final String filepath = "src/main/resources/data/sirv.csv";

    @Autowired
    private final ImageService imageService;


    public void updateProductImages() {
        List<Product> products = productRepository.findAll();
        ImageUrls imageUrls;
        for (Product product : products) {
            imageUrls = new ImageUrls();
            try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] columns = line.split(",");

                    if (columns.length >= 2) {
                        if (columns[1].contains(" ")) continue;
                        if (columns[1].contains(product.getSku()) && columns[1].contains(".jpg")) {
                            int skuLastIndex = columns[1].lastIndexOf(product.getSku()) + product.getSku().length();
                            if (skuLastIndex < columns[1].length()) {
                                char nextChar = columns[1].charAt(skuLastIndex);

                                // Check if the next character is a letter or digit
                                if (Character.isLetterOrDigit(nextChar)) {
                                    // Skip processing if next character is letter or digit
                                    continue;
                                }
                            }
                            if (columns[1].contains("/DIM/")) {
                                imageUrls.getDim().add(columns[1]);
                            } else if (columns[1].contains("/CGI/")) {
                                imageUrls.getCgi().add(columns[1]);
                            } else {
                                if (imageUrls.getImg().contains(columns[1])) continue;
                                imageUrls.getImg().add(columns[1]);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading the CSV file: " + e.getMessage());
            }
            String imagesJsonString = imageService.buildJsonString(imageUrls);
            product.setImages(imagesJsonString);
            productRepository.save(product);
            System.out.println("SAVED : " + product.getSku() + "/n IMG : " + imagesJsonString);
        }
    }

    public void updateComponentImages(){
        List<Component> components = componentRepository.findAll();
        ImageUrls imageUrls;
        for (Component component : components) {
            imageUrls = new ImageUrls();
            try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] columns = line.split(",");

                    if (columns.length >= 2) {
                        if (columns[1].contains(" ")) continue;

                        if (columns[1].contains(component.getSku()) && columns[1].contains(".jpg")) {

                            int skuLastIndex = columns[1].lastIndexOf(component.getSku()) + component.getSku().length();
                            if (skuLastIndex < columns[1].length()) {
                                char nextChar = columns[1].charAt(skuLastIndex);

                                // Check if the next character is a letter or digit
                                if (Character.isLetterOrDigit(nextChar)) {
                                    // Skip processing if next character is letter or digit
                                    continue;
                                }
                            }


                            if (columns[1].contains("/DIM/")) {
                                imageUrls.getDim().add(columns[1]);
                            } else if (columns[1].contains("/CGI/")) {
                                imageUrls.getCgi().add(columns[1]);
                            } else {
                                if (imageUrls.getImg().contains(columns[1])) continue;
                                imageUrls.getImg().add(columns[1]);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading the CSV file: " + e.getMessage());
            }
            String imagesJsonString = imageService.buildJsonString(imageUrls);
            component.setImages(imagesJsonString);
            componentRepository.save(component);
            System.out.println("SAVED : " + component.getSku() + "/n IMG : " + imagesJsonString);
        }
    }
}