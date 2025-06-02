package com.danny.ewf_service.converter;


import com.danny.ewf_service.entity.BayLocation;
import com.danny.ewf_service.payload.response.BayLocationResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IBayLocationMapper {

    @Mapping(target = "bayCode", source = "bayLocation.bayCode")
    @Mapping(target = "zone", source = "bayLocation.zone")
    @Mapping(target = "maxPallets", source = "bayLocation.maxPallets")
    BayLocationResponseDto bayLocationToBayLocationResponseDto(BayLocation bayLocation);
    List<BayLocationResponseDto> bayLocationToBayLocationResponseDtos(List<BayLocation> bayLocations);
}
