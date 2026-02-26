package com.danny.ewf_service.utils.exports;

import com.danny.ewf_service.entity.Component;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.entity.product.ProductComponent;
import com.danny.ewf_service.entity.product.ProductDetail;
import com.danny.ewf_service.repository.ComponentRepository;
import com.danny.ewf_service.repository.ProductComponentRepository;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.service.ProductService;
import com.danny.ewf_service.service.impl.ProductServiceImpl;
import com.danny.ewf_service.utils.CsvWriter;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductExport {

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final CsvWriter csvWriter;

    @Autowired
    private final ComponentRepository componentRepository;

    @Autowired
    private final ProductService productService;

    @Autowired
    private final ProductComponentRepository productComponentRepository;

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

    public void exportComponent(String filePath){
        List<Component> allComponents = componentRepository.findAll();

        System.out.println("Found " + allComponents.size() + " components");
        List<String[]> rows = new ArrayList<>();
        for (Component component : allComponents) {
            rows.add(new String[]{component.getSku()});
        }
        csvWriter.exportToCsv(rows, filePath);
    }

    public void exportMergedProducts(String filePath){
        List<Product> products = productRepository.findAllProducts();
        List<String[]> rows = new ArrayList<>();
        for (Product product : products) {
            List<Product> mergedProducts = productService.findMergedProducts(product);
            List<String> subSingleSkus = productComponentRepository.findSingleProductsByProductSku(product.getId());
            if (mergedProducts != null) {
                for (Product mergedProduct : mergedProducts) {
                    rows.add(new String[]{product.getSku(), mergedProduct.getSku()});
                }

                System.out.println("Found " + mergedProducts.size() + " merged products for " + product.getSku());
            }
            for (String subSingleSku : subSingleSkus) {
                rows.add(new String[]{product.getSku(), subSingleSku});
            }
            System.out.println("Found " + subSingleSkus.size() + " sub single products for " + product.getSku());
        }

        csvWriter.exportToCsv(rows, filePath);
    }

    public void exportProductWithDimension(String filePath) {
        List<Product> products = productRepository.findAllProducts();
        List<String[]> rows = new ArrayList<>();
        for (Product product : products) {
            System.out.println("Processing " + product.getSku());
            List<String> rowList = new ArrayList<>();
            rowList.add(product.getSku());
            if (product.getComponents() != null) {

                moveComponentsWithQuantityBoxGreaterThanOneToThirdPosition(product.getComponents());
               for (ProductComponent productComponent : product.getComponents()) {
                    rowList.add(productComponent.getComponent().getSku());
                    if (productComponent.getComponent().getDimension() != null) {
                        rowList.add(String.valueOf(Math.ceil((double) productComponent.getQuantity() / productComponent.getComponent().getDimension().getQuantityBox())));
                        rowList.add(String.valueOf(Math.ceil(productComponent.getComponent().getDimension().getBoxLength())));
                        rowList.add(String.valueOf(Math.ceil(productComponent.getComponent().getDimension().getBoxWidth())));
                        rowList.add(String.valueOf(Math.ceil(productComponent.getComponent().getDimension().getBoxHeight())));
                        rowList.add(String.valueOf(Math.ceil(productComponent.getComponent().getDimension().getBoxWeight())));
                    }
                    else {
                        rowList.add("");
                        rowList.add("");
                        rowList.add("");
                        rowList.add("");
                        rowList.add("");
                    }
                }
            }
            rows.add(rowList.toArray(new String[0]));
        }
        csvWriter.exportToCsv(rows, filePath);
    }
    public void moveComponentsWithQuantityBoxGreaterThanOneToThirdPosition(List<ProductComponent> components) {
        ProductComponent maxBoxLengthComponent = null;
        double maxBoxLength = Double.MIN_VALUE;

        List<Long> componentIds = components.stream()
                .filter(pc -> pc.getComponent().getSku().contains("DSP") || pc.getComponent().getSku().contains("DSL"))
                .map(pc -> pc.getComponent().getId())
                .toList();
        if (!componentIds.isEmpty()) {
            Optional<ProductServiceImpl.ProductMergedProjection> result = productComponentRepository.findProductByExactComponents(componentIds, componentIds.size());
            if (result.isPresent()) {
                for (ProductComponent pc : components) {
                    if (pc.getComponent().getSku().contains("DSP")) {
                        pc.getComponent().setSku(result.get().getSku());
                        break;
                    }
                }

            }
        }

        // Find components with quantityBox > 1
        components.removeIf(productComponent -> productComponent.getComponent().getSku().contains("DSP")
                                                || productComponent.getComponent().getSku().contains("DSL"));

        for (ProductComponent productComponent : components) {
            if (productComponent.getComponent() != null
                && productComponent.getComponent().getDimension() != null
                && productComponent.getComponent().getDimension().getBoxLength() != null) {
                double boxLength = productComponent.getComponent().getDimension().getBoxLength();
                if (boxLength > maxBoxLength) {
                    maxBoxLength = boxLength;
                    maxBoxLengthComponent = productComponent;
                }
            }
        }
        // If a component with a valid BoxLength was found, move it to the first index
        if (maxBoxLengthComponent != null) {
            components.remove(maxBoxLengthComponent);
            components.add(0, maxBoxLengthComponent);
        }


        for (ProductComponent productComponent : components) {
            if (productComponent.getComponent() != null
                && productComponent.getComponent().getDimension() != null
                && productComponent.getComponent().getDimension().getQuantityBox() > 1) {
                components.remove(productComponent);
                components.add(Math.min(components.size(),2), productComponent);
                return;
            }
        }
    }
}
