package com.danny.ewf_service.converter;


import com.danny.ewf_service.entity.LPN;
import com.danny.ewf_service.payload.request.LpnRequestDto;
import com.danny.ewf_service.payload.response.LpnResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ILpnMapper {

    @Mapping(target = "tagID", source = "lpnRequestDto.tagID")
    @Mapping(target = "quantity", source = "lpnRequestDto.quantity")
    @Mapping(target = "containerNumber", source = "lpnRequestDto.containerNumber")
    @Mapping(target = "date", source = "lpnRequestDto.date")
    LPN lpnRequestDtoToLpn(LpnRequestDto lpnRequestDto);


    @Mapping(target = "tagID", source = "lpn.tagID")
    @Mapping(target = "quantity", source = "lpn.quantity")
    @Mapping(target = "containerNumber", source = "lpn.containerNumber")
    @Mapping(target = "bayCode", source = "lpn.bayLocation.bayCode")
    @Mapping(target = "zone", source = "lpn.bayLocation.zone")
    @Mapping(target = "status", source = "lpn.status")
    @Mapping(target = "date", source = "lpn.date")
    LpnResponseDto lpnToLpnResponseDto(LPN lpn);
    List<LpnResponseDto> lpnListToLpnResponseDtoList(List<LPN> lpnList);
}
