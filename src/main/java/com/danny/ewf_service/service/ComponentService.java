package com.danny.ewf_service.service;

import com.danny.ewf_service.entity.Product;
import com.danny.ewf_service.payload.response.ComponentResponseDto;

import java.util.List;

public interface ComponentService {

    List<ComponentResponseDto>  findComponents (Product product);
}
