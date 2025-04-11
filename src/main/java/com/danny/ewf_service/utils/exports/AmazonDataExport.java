package com.danny.ewf_service.utils.exports;

import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.entity.product.ProductMetadata;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.utils.CsvWriter;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class AmazonDataExport {

    private final String searchApiToken;

    private final CsvWriter csvWriter;

    private final ProductRepository productRepository;

    private final RestTemplate restTemplate;


    public AmazonDataExport(
            @Value("${searchapi.token}") String searchApiToken,
            CsvWriter csvWriter,
            ProductRepository productRepository) {
        this.searchApiToken = searchApiToken;
        this.csvWriter = csvWriter;
        this.productRepository = productRepository;
        this.restTemplate = new RestTemplate();
    }

    public void extractDataFromAmazon(){
        List<Product> products = productRepository.findAllByAsinIsNotNull();
        System.out.println("Found " + products.size() + " products with ASIN");
        String searchApiRequest = "https://www.searchapi.io/api/v1/search?api_key=" + searchApiToken + "&asin=";
        ProductMetadata metadata;
        for (Product product : products) {
            metadata = product.getMetadata();
            if (metadata == null) {
                metadata = new ProductMetadata();
                try {
                    String apiResponse = restTemplate.getForObject(searchApiRequest + product.getAsin() + "&engine=amazon_product", String.class);
                    metadata.setAmzSearchapi(apiResponse);
                    product.setMetadata(metadata);
                    productRepository.save(product);
                    System.out.println("Saved metadata for " + product.getSku() + " with ASIN " + product.getAsin());
                    return;
                } catch (Exception e) {
                    System.err.println("Error fetching data for ASIN " + product.getAsin() + ": " + e.getMessage());
                }
            }
        }
    }

}
