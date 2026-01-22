package com.danny.ewf_service.utils.exports;

import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.utils.CsvWriter;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ProductExport {

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final CsvWriter csvWriter;

    public void exportProduct(String filePath){
        List<Product> products = productRepository.findAllProducts();
        List<String[]> rows = new ArrayList<>();

        String[] header = {"SKU", "ASIN", "Title"};
        rows.add(header);
        for (Product product : products) {
            if (product.getAsin() != null) {
                if (product.getAsin().isEmpty()) continue;
                rows.add(new String[]{product.getSku(), product.getAsin(),product.getTitle(), "https://www.amazon.com/dp/" + product.getAsin()});
            }
        }

        csvWriter.exportToCsv(rows, filePath);
    }

}
