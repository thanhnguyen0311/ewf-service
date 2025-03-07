package com.danny.ewf_service.converter;

import com.danny.ewf_service.entity.Component;
import com.danny.ewf_service.payload.response.ComponentResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IComponentMapper {

    @Mapping(target = "id", source = "component.id")
    @Mapping(target = "sku", source = "component.sku")
    @Mapping(target = "image", source = "component.images")
    @Mapping(target = "finish", source = "component.finish")
    @Mapping(target = "category", source = "component.category")
    @Mapping(target = "inventory", source = "component.inventory")
    ComponentResponseDto componentToComponentResponseDto(Component component);

}
