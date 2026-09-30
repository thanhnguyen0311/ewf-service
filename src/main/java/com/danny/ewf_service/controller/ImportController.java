package com.danny.ewf_service.controller;

import com.danny.ewf_service.service.impl.DfShippingLabelService;
import com.danny.ewf_service.repository.BayLocationRepository;
import com.danny.ewf_service.service.ClaudeService;
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

    @Autowired
    private final ClaudeService claudeService;



    @GetMapping("/data")
    public ResponseEntity<?> importData() {
        try {
            String filepath = "/data/skus.csv";
            String filepath2 = "/data/product_report_day.csv";
            String accessToken = "Atza|IwEBIAv3zFKyDx9YLqKoIdlTnHJkIUJ-FoW8Us1It148rC-44ReS3KnZ_BTbB5cB32b7v7feaNt-_PhvYer2dLfiOVoLYraya-mi6sz3Rrcism17dWNT1qR2lkAewym0T9UKO1_XQltAK4AHhp_b6Cq0adkDZpO_B3up5LEqpz4ZBbfles08nQWin8197zruMaV0Gh55Ni9rOlxlJGnPajU6qtTstHSe-g8M6lkkWMDH-fpG4I-mfJu1Bb-AF_3rh3W86TBofSRFQAdiFly-9dOdjB5LmZY3XEvm6zVHCJzU7ofzon-QL6JQl5g8oV_43McA_fl1nRwdjMnsQ6njjyV4aA0U";   // Atza|...



//            List<Product> products = productService.getListProductFromCsvFile("src/main/resources/data/chairs.csv");
//            imagesImport.updateProductImages(products);
//            shopifyExport.exportProductListing(products,"new_product.csv",true);
//            shopifyExport.exportProductCustomfields(products,"new_product_customfields.csv");

//            imagesImport.updateComponentImages();
//            productService.getListProductFromCsvFile("src/main/resources/data/skus.csv");
//            shopifyExport.exportProductListing();
//            wayfairReportImport.importWayfairReportDaily(filepath2);
//            productService.generateProductMetaData();
//            spreadsheetService.updateProductData(new String[]{
//                    "Type", "Category", "Shipping", "Main Category", "Luxe", "Group ID", "UPC", "Finish", "PIECES", "Chair Type", "Size & Shape", "Style", "Collection", "ASIN", "", "Sub Category","Title","Description", "HTML Description"
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
