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
    @Mapping(target = "inventory", source = "component.inventory")
    @Mapping(target = "manufacturer", source = "component.manufacturer")
    @Mapping(target = "name", source = "component.name")
    @Mapping(target = "discontinue", source = "component.discontinue")
    ComponentInventoryResponseDto componentToComponentInventoryResponseDto(Component component);
    List<ComponentInventoryResponseDto> componentListToComponentInventoryResponseDtoList(List<Component> components);

    @Named("extractImages")
    default ImageUrls extractImages(String imagesJson) {
        return new ImageUrls().parseImageJson(imagesJson);
    }
}
