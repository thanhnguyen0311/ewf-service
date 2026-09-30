package com.danny.ewf_service.service;

import com.danny.ewf_service.payload.request.product.ProductMetaDto;
import com.danny.ewf_service.payload.response.product.ProductMetaResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;

public interface ClaudeService {

    ProductMetaResponseDto getProductMeta(ProductMetaDto productMetaDto) throws IOException, InterruptedException;
}
