package com.danny.ewf_service.controller;

import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.repository.BayLocationRepository;
import com.danny.ewf_service.service.LpnService;
import com.danny.ewf_service.service.ProductService;
import com.danny.ewf_service.utils.CsvWriter;
import com.danny.ewf_service.utils.exports.AmazonDataExport;
import com.danny.ewf_service.utils.exports.ProductExport;
import com.danny.ewf_service.utils.exports.ShopifyExport;
import com.danny.ewf_service.utils.exports.WMSExport;
import com.danny.ewf_service.utils.imports.ComponentsImport;
import com.danny.ewf_service.utils.imports.ImagesImport;
import com.danny.ewf_service.utils.imports.ProductsImport;
import com.danny.ewf_service.utils.imports.WayfairReportImport;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequestMapping("/import")
@RestController
@AllArgsConstructor
public class ImportController {
    @Autowired
    private final AmazonDataExport amazonDataExport;

    @Autowired
    private final ShopifyExport shopifyExport;

    @Autowired
    private final ProductsImport productsImport;

    @Autowired
    private final CsvWriter csvWriter;

    @Autowired
    private final ComponentsImport componentsImport;

    @Autowired
    private final ImagesImport imagesImport;

    @Autowired
    private final BayLocationRepository bayLocationRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private LpnService lpnService;

    @Autowired
    private WayfairReportImport wayfairReportImport;

    @Autowired
    private WMSExport wmsExport;

    @Autowired
    private final ProductExport productExport;



    @GetMapping("/data")
    public ResponseEntity<?> importData() {
        try {
            String filepath = "/data/product_report_day.csv";
//            List<Product> products = productService.getListProductFromCsvFile("src/main/resources/data/skus.csv");
//            shopifyExport.exportProductListing(products, "products.csv", true);
//            shopifyExport.exportProductCustomfields(products, "products_customsfield.csv");
//            productsImport.importProductDetails();
//            componentsImport.importPrices();
//            componentsImport.importDimensions();
//            shopifyExport.exportShopifyProductsPrice("product_prices_02-02.csv");
//            wayfairReportImport.importWayfairReportKeywordDaily(filepath);
//            productsImport.importProductDetails();
//                shopifyExport.exportAmazonReviews();
//            wmsExport.exportSKU("wms.csv");
//            wayfairReportImport.importWayfairParentSkuProduct(filepath);
//            productExport.exportProduct("wayfair_reviews.csv");
//                amazonDataExport.extractDataFromAmazon();
//            shopifyExport.exportShopifyProductsPrice(filepath);

            wayfairReportImport.importWayfairReportDaily(filepath);
//                productsImport.importProductPrice();
            return ResponseEntity.ok().body("SUCCESS");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }
}
