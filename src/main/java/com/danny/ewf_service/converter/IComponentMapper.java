package com.danny.ewf_service.converter;

import com.danny.ewf_service.entity.Component;
import com.danny.ewf_service.entity.ImageUrls;
import com.danny.ewf_service.payload.response.ComponentInventoryResponseDto;
import com.danny.ewf_service.payload.response.ComponentResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IComponentMapper {

    @Mapping(target = "id", source = "component.id")
    @Mapping(target = "sku", source = "component.sku")
    @Mapping(target = "images", source = "component.images", qualifiedByName = "extractImages")
    @Mapping(target = "finish", source = "component.finish")
    @Mapping(target = "category", source = "component.category")
    @Mapping(target = "inventory", source = "component.inventory")
    ComponentResponseDto componentToComponentResponseDto(Component component);
    List<ComponentResponseDto> componentListToComponentResponseDtoList(List<Component> components);

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
    @Mapping(target = "inTransit", source = "component.report.inTransit")
    @Mapping(target = "inStock", source = "component", qualifiedByName = "calculateInStock")
    @Mapping(target = "discontinue", source = "component.discontinue")
    @Mapping(target = "report120Days", source = "component.report.salesReport", qualifiedByName = "calculate120DaysSale")
    @Mapping(target = "rating", source = "component", qualifiedByName = "calculateRating")
    ComponentInventoryResponseDto componentToComponentInventoryResponseDto(Component component);
    List<ComponentInventoryResponseDto> componentListToComponentInventoryResponseDtoList(List<Component> components);

    @Named("extractImages")
    default ImageUrls extractImages(String imagesJson) {
        return new ImageUrls().parseImageJson(imagesJson);
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
        long inStock = component.getInventory() - component.getReport().getOnPO();
        long c120days = component.getReport().getSalesReport()*2+1;
        double rating = (double) (inStock + component.getReport().getInTransit()) /c120days;
        return Math.round(rating * 10) / 10.0;
    }
}
