package com.danny.ewf_service.utils.exports;

import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.entity.product.ProductDetail;
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

        String[] header = {"SKU", "ASIN", "Title", "EWFDirect", "Description", "Pieces", "Collection", "Size & Shape", "Finish" , "Category" };
        rows.add(header);
        ProductDetail productDetail;
        String description;
        String pieces;
        String collection;
        String sizeShape;
        String finish;
        String category;
        String link;
        for (Product product : products) {
            description = "";
            pieces = "";
            collection = "";
            sizeShape = "";
            finish = "";
            category = "";
            link = "";
            if (product.getAsin() != null) {
                if (product.getAsin().isEmpty()) continue;
                if (product.getProductDetail() != null) {
                    productDetail = product.getProductDetail();
                    if (productDetail.getDescription() != null) description = productDetail.getDescription();
                    if (productDetail.getPieces() != null) pieces = productDetail.getPieces();
                    if (productDetail.getCollection() != null) collection = productDetail.getCollection();
                    if (productDetail.getSizeShape() != null) sizeShape = productDetail.getSizeShape();
                    if (productDetail.getFinish() != null) finish = productDetail.getFinish();
                    if (productDetail.getMainCategory() != null) category = productDetail.getMainCategory().concat(" - ");
                    if (productDetail.getSubCategory() != null) category = category.concat(productDetail.getSubCategory());
                }

                if (product.getWholesales() != null) {
                    if (product.getWholesales().getEwfdirect()) link = "https://www.ewfdirect.com/products/" + product.getSku().toLowerCase();
                }
                rows.add(new String[]{product.getSku(), product.getAsin(),product.getTitle(), link, description, pieces, collection, sizeShape, finish, category});
            }
        }

        csvWriter.exportToCsv(rows, filePath);
    }

}
