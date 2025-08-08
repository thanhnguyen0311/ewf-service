package com.danny.ewf_service.converter;

import com.danny.ewf_service.entity.Component;
import com.danny.ewf_service.entity.ImageUrls;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.payload.response.component.ComponentInboundResponseDto;
import com.danny.ewf_service.payload.response.component.ComponentInventoryResponseDto;
import com.danny.ewf_service.payload.response.component.ComponentListWMSResponse;
import com.danny.ewf_service.payload.response.component.ComponentResponseDto;
import com.danny.ewf_service.payload.response.product.ProductPriceResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IComponentMapper {

    @Mapping(target = "sku", source = "component.sku")
    @Mapping(target = "upc", source = "component.upc")
    @Mapping(target = "palletCapacity", source = "component.dimension.palletCapacity")
    ComponentInboundResponseDto componentToComponentInboundResponseDto(Component component);
    List<ComponentInboundResponseDto> componentListToComponentInboundResponseDtoList(List<Component> components);

    @Mapping(target = "id", source = "component.id")
    @Mapping(target = "sku", source = "component.sku")
    @Mapping(target = "images", source = "component.images", qualifiedByName = "extractImages")
    @Mapping(target = "finish", source = "component.finish")
    @Mapping(target = "category", source = "component.category")
    @Mapping(target = "inventory", source = "component.inventory")
    ComponentResponseDto componentToComponentResponseDto(Component component);
    List<ComponentResponseDto> componentListToComponentResponseDtoList(List<Component> components);

    @Mapping(target = "sku", source = "component.sku")
    @Mapping(target = "QB1", source = "component.price.QB1")
    @Mapping(target = "QB3", source = "component.price.QB3")
    @Mapping(target = "QB7", source = "component.price.QB7")
    ProductPriceResponseDto componentToProductPriceResponseDto(Component component);


    @Mapping(target = "id", source = "component.id")
    @Mapping(target = "sku", source = "component.sku")
    @Mapping(target = "upc", source = "component.upc")
    @Mapping(target = "name", source = "component.name")
    @Mapping(target = "type", source = "component.subType")
    @Mapping(target = "images", source = "component.images", qualifiedByName = "extractImagesToList")
    @Mapping(target = "dimension", source = "component.dimension")
    @Mapping(target = "manufacturer", source = "component.manufacturer")
    ComponentListWMSResponse componentToComponentListWMSResponseDto(Component component);
    List<ComponentListWMSResponse> componentListToComponentListWMSResponseDtoList(List<Component> components);


    @Mapping(target = "id", source = "component.id")
    @Mapping(target = "sku", source = "component.sku")
    @Mapping(target = "images", source = "component.images", qualifiedByName = "extractImages")
    @Mapping(target = "finish", source = "component.finish")
    @Mapping(target = "category", source = "component.category")
    @Mapping(target = "salesReport", source = "component.report.salesReport")
    @Mapping(target = "inventory", source = "component.inventory")
    @Mapping(target = "manufacturer", source = "component.manufacturer")
    @Mapping(target = "name", source = "component.name")
    @Mapping(target = "onPO", source = "component.report.onPO")
    @Mapping(target = "toShip", source = "component.report.toShip")
    @Mapping(target = "stockVN", source = "component.report.stockVN")
    @Mapping(target = "inProduction", source = "component.report.inProduction")
    @Mapping(target = "inTransit", source = "component.report.inTransit")
    @Mapping(target = "discontinue", source = "component.discontinue")
    @Mapping(target = "inStock", source = "component", qualifiedByName = "calculateInStock")
    @Mapping(target = "report120Days", source = "component.report.salesReport", qualifiedByName = "calculate120DaysSale")
    @Mapping(target = "rating", source = "component", qualifiedByName = "calculateRating")
    @Mapping(target = "stockStatus", source = "component", qualifiedByName = "calculateStockStatus")
    ComponentInventoryResponseDto componentToComponentInventoryResponseDto(Component component);
    List<ComponentInventoryResponseDto> componentListToComponentInventoryResponseDtoList(List<Component> components);

    @Named("extractImages")
    default ImageUrls extractImages(String imagesJson) {
        ImageUrls imageUrl = new ImageUrls();
        imageUrl.parseImageJson(imagesJson);
        return imageUrl;
    }

    @Named("extractImagesToList")
    default List<String> extractImagesToList(String imagesJson) {
        ImageUrls imageUrl = new ImageUrls();
        imageUrl.parseImageJson(imagesJson);
        return imageUrl.toList();
    }

    @Named("calculate120DaysSale")
    default Long calculate120DaysSale(Long salesReport) {
        return salesReport*2+1;
    }

    @Named("calculateInStock")
    default Long calculateInStock(Component component) {
        return component.getInventory() - component.getReport().getOnPO();
    }

    @Named("calculateRating")
    default Double calculateRating(Component component) {
        double rating = (double) (calculateInStock(component) + component.getReport().getInTransit()) /calculate120DaysSale(component.getReport().getSalesReport());
        return Math.round(rating * 10) / 10.0;
    }

    @Named("calculateStockStatus")
    default String calculateStockStatus(Component component) {
        try {
            long inStock = calculateInStock(component);
            long calculate120days = calculate120DaysSale(component.getReport().getSalesReport());
            double sum = inStock
                    + calculateRating(component)
                    + component.getReport().getInTransit()
                    + component.getReport().getToShip();
            double ratio = sum / calculate120days;
            if (component.getDiscontinue()) {
                return "P-Không SX";
            } else if (sum == 0) {
                return (calculate120days > sum) ? "A-Đưa Vào SX" : "";
            } else if (ratio > 3) {
                return "O-Hơn 1 năm";
            } else if (ratio > 2.5) {
                return "N-10 Tháng";
            } else if (ratio > 2) {
                return "M-8 Tháng";
            } else if (ratio > 1.5) {
                return "L-6 Tháng";
            } else if (ratio > 1) {
                return "K-4 Tháng";
            } else if (ratio > 0.75) {
                return "I-3 tháng";
            } else if (ratio > 0.63) {
                return "H-2.5 Tháng";
            } else if (ratio > 0.5) {
                return "G-2 Tháng";
            } else if (ratio > 0.38) {
                return "E-45 Ngày";
            } else if (ratio > 0.25) {
                return "D-30 Ngày";
            } else if (ratio > 0.13) {
                return "C-15 Ngày";
            } else {
                return "B-Hết Hàng";
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return "B-Hết Hàng";
    }
}
