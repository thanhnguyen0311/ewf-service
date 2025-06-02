package com.danny.ewf_service.converter;


import com.danny.ewf_service.entity.LPN;
import com.danny.ewf_service.payload.request.LpnRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ILpnMapper {

    @Mapping(target = "tagID", source = "lpnRequestDto.tagID")
    @Mapping(target = "quantity", source = "lpnRequestDto.quantity")
    @Mapping(target = "containerNumber", source = "lpnRequestDto.containerNumber")
    @Mapping(target = "date", source = "lpnRequestDto.date")
    LPN lpnRequestDtoToLpn(LpnRequestDto lpnRequestDto);
}
