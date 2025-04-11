package com.danny.ewf_service.utils.exports;

import com.danny.ewf_service.entity.Price;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.entity.product.ProductMetadata;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.utils.CsvWriter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Service
public class AmazonDataExport {

    private final String searchApiToken;

    private final CsvWriter csvWriter;

    private final ProductRepository productRepository;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;



    public AmazonDataExport(
            @Value("${searchapi.token}") String searchApiToken,
            CsvWriter csvWriter,
            ProductRepository productRepository) {
        this.searchApiToken = searchApiToken;
        this.csvWriter = csvWriter;
        this.productRepository = productRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public void extractDataFromAmazon(){
        List<Product> products = productRepository.findAllByAsinIsNotNull();
        System.out.println("Found " + products.size() + " products with ASIN");
        String searchApiRequest = "https://www.searchapi.io/api/v1/search?api_key=" + searchApiToken + "&asin=";
        ProductMetadata metadata;
        String apiResponse;
        for (Product product : products) {
            if (product.getProductDetail() != null) {
                if (Objects.equals(product.getProductDetail().getMainCategory(), "Outdoor")) continue;
            }

            metadata = product.getMetadata();
            if (metadata == null) metadata = new ProductMetadata();
            if (metadata.getAmzSearchapi() == null) {
                try {
                    apiResponse = restTemplate.getForObject(searchApiRequest + product.getAsin() + "&engine=amazon_product", String.class);
                    metadata.setAmzSearchapi(apiResponse);
                    product.setMetadata(metadata);
                    productRepository.save(product);
                } catch (Exception e) {
                    System.err.println("Error fetching data for ASIN " + product.getAsin() + ": " + e.getMessage());
                }
            }

            Price price = product.getPrice();
            if (price == null) price = new Price();

            double amazonPrice = extractPriceValueFromJson(metadata.getAmzSearchapi());
            if (amazonPrice != 0.0) {
                price.setAmazonPrice(amazonPrice);
                System.out.println("Saved Amazon price for " + product.getSku() + " with ASIN " + product.getAsin() + ": " + amazonPrice);
            }
        }
    }

    private Double extractPriceValueFromJson(String jsonResponse) {

        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            // Navigate to the buybox.price.value node
            JsonNode productNode = rootNode.path("product");
            if (!productNode.isMissingNode()) {
                JsonNode buyboxNode = productNode.path("buybox");
                if (!buyboxNode.isMissingNode()) {
                    JsonNode priceNode = buyboxNode.path("price");
                    if (!priceNode.isMissingNode()) {
                        JsonNode valueNode = priceNode.path("value");
                        if (!valueNode.isMissingNode() && valueNode.isNumber()) {
                            return valueNode.asDouble();
                        }
                    }
                }
            }
        } catch (Exception e) {
            return 0.0;
        }
        return 0.0;
    }

}
