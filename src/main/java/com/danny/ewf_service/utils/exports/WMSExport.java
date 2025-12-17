package com.danny.ewf_service.utils.exports;

import com.danny.ewf_service.entity.Component;
import com.danny.ewf_service.entity.ImageUrls;
import com.danny.ewf_service.repository.ComponentRepository;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.utils.CsvWriter;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class WMSExport {

    @Autowired
    private final ComponentRepository componentRepository;

    @Autowired
    private final CsvWriter csvWriter;

    public void exportSKU(String filePath){
        List<String[]> rows = new ArrayList<>();
        String[] header = {"SKU", "UPC", "Name", "Manufacturer", "Type", "Size Shape", "Quantity Box", "Width", "Height", "Length", "Weight", "Pallet Capacity", "Image Url"};
        rows.add(header);
        List<Component> components = componentRepository.findAllComponents();
        ImageUrls imageUrls;

        for (Component component : components) {
            System.out.println("Processing " + component.getSku());
            imageUrls = new ImageUrls().parseImageJson(component.getImages());
            rows.add(new String[]{
                    component.getSku(),
                    component.getUpc(),
                    component.getName(),
                    component.getManufacturer(),
                    component.getSubType(),
                    component.getDimension() != null ? component.getDimension().getSizeShape() : "",
                    component.getDimension() != null ? String.valueOf(component.getDimension().getQuantityBox()) : "",
                    component.getDimension() != null ? String.valueOf(component.getDimension().getBoxWidth()) : "",
                    component.getDimension() != null ? String.valueOf(component.getDimension().getBoxHeight()) : "",
                    component.getDimension() != null ? String.valueOf(component.getDimension().getBoxLength()) : "",
                    component.getDimension() != null ? String.valueOf(component.getDimension().getBoxWeight()) : "",
                    component.getDimension() != null ? (component.getDimension().getPalletCapacity() != null ? String.valueOf(component.getDimension().getPalletCapacity()) : "") : "",
                    !imageUrls.getCgi().isEmpty() ? imageUrls.getCgi().get(0) : "",

            });
        }

        csvWriter.exportToCsv(rows, filePath);
    }
}
