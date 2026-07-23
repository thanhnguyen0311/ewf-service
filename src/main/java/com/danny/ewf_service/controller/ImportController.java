package com.danny.ewf_service.controller;

import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.repository.BayLocationRepository;
import com.danny.ewf_service.service.LpnService;
import com.danny.ewf_service.service.ProductService;
import com.danny.ewf_service.service.SpreadsheetService;
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

import java.util.ArrayList;
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


    @Autowired
    private final SpreadsheetService spreadsheetService;



    @GetMapping("/data")
    public ResponseEntity<?> importData() {
        try {
            String filepath = "/data/product_report_day.csv";
            String filepath2 = "/data/product_report_day.csv";

//            imagesImport.updateProductImages(new ArrayList<>());
//            imagesImport.updateComponentImages();
//            productService.getListProductFromCsvFile("src/main/resources/data/skus.csv");
//            shopifyExport.exportProductListing();
//            wayfairReportImport.importWayfairReportDaily(filepath);
//            spreadsheetService.updateProductData(new String[]{
//                    "Title","Description", "HTML Description"
//
//            });
//            productsImport.updateSaleChannel("src/main/resources/data/skus.csv");
//            shopifyExport.exportProductCustomLabel("custom_label.csv");
//            shopifyExport.exportShopifyProductsPrice("shopify_products_price_06_23.csv");
//            shopifyExport.exportProductType("product_type.csv");

            return ResponseEntity.ok().body("SUCCESS");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }
}
