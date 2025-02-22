package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.Customer;
import com.danny.ewf_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
