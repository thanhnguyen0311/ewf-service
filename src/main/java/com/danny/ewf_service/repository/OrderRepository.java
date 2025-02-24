package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByInvoiceNumber(String invoiceNumber);

}
