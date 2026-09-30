package com.danny.ewf_service.service;


import com.danny.ewf_service.service.impl.DfShippingLabelService;
import com.danny.ewf_service.payload.response.OrderListResponseDto;
import net.sourceforge.tess4j.TesseractException;

import java.io.IOException;
import java.util.List;


public interface OrderService {

    List<OrderListResponseDto> getAllOrders();

    List<DfShippingLabelService.LabelDetails> getLabels(String PODNumber, String accessToken) throws TesseractException, IOException, InterruptedException;
}
