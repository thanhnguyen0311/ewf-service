package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.converter.IOrderMapper;
import com.danny.ewf_service.entity.Order;
import com.danny.ewf_service.payload.request.OrderRequestDto;
import com.danny.ewf_service.payload.response.OrderListResponseDto;
import com.danny.ewf_service.repository.OrderRepository;
import com.danny.ewf_service.service.OrderService;
import lombok.AllArgsConstructor;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.data.domain.Page;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    @Autowired
    private final OrderRepository orderRepository;

    @Autowired
    private final IOrderMapper IOrderMapper;

    @Override
    public List<OrderListResponseDto> getAllOrders() {
        PageRequest pageRequest = PageRequest.of(0, 1000, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<Order> orderPage = orderRepository.findAllByChannel("unknown",pageRequest);

        List<Order> orders = orderPage.getContent();

        return IOrderMapper.orderToOrderListResponseDtos(orders);
    }

    @Override
    public void updateAmzOrders(List<OrderRequestDto> orderRequestDtos) {
        for (OrderRequestDto orderRequestDto : orderRequestDtos) {
            Order order = new Order();
            order.setChannel("amazon");
            order.setContactName(orderRequestDto.getContactName());
            order.setAddress1(orderRequestDto.getAddress1());
            order.setAddress2(orderRequestDto.getAddress2());
            order.setCity(orderRequestDto.getCity());
            order.setState(orderRequestDto.getState());
            order.setGroupSku(orderRequestDto.getGroupSku());
            order.setTotalPrice(orderRequestDto.getPrices());
            order.setCustomer(orderRequestDto.getContactName());
            order.setPhone(orderRequestDto.getPhone());
            order.setPoNumber(orderRequestDto.getPoNumber());
            order.setQuantity(orderRequestDto.getQuantity());
            order.setZipcode(orderRequestDto.getZip());
            order.setMasterTrackingNumber(orderRequestDto.getTrackingNumber());
            order.setTrackingNumber(orderRequestDto.getTrackingNumber());
            order.setCarrier(orderRequestDto.getCarrier());
            orderRepository.save(order);
        }

    }

    @Override
    public List<DfShippingLabelService.LabelDetails> getLabels(String PODNumber, String accessToken) throws TesseractException, IOException, InterruptedException {
        DfShippingLabelService service = new DfShippingLabelService();
        List<DfShippingLabelService.LabelDetails> labels = service.getLabelDetailsByPo(PODNumber, accessToken);

        for (DfShippingLabelService.LabelDetails d : labels ) {
            System.out.println("PO:        " + d.poNumber());
            System.out.println("Package:   " + d.packageId() + " (" + d.packageOf() + ")");
            System.out.println("Tracking:  " + d.trackingNumber());
            System.out.println("Customer:  " + d.customerName());
            System.out.println("Address:   " + d.addressLine() + ", " + d.city() + " " + d.state() + " " + d.zip());
            System.out.println("DWT:       " + d.dwt());
            System.out.println("Weight:    " + d.weightLbs() + " LBS");
            System.out.println();
        }
        return labels;
    }

    public List<OrderListResponseDto> sortOrdersByUpdatedAt(List < OrderListResponseDto> orderDtoList) {
        return orderDtoList.stream()
                .sorted(Comparator.comparing(OrderListResponseDto::getUpdatedAt).reversed()) // Sort by updatedAt in descending order
                .collect(Collectors.toList());
    }
}
