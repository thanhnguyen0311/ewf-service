package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByPhone(String phone); //

    List<Customer> findByPhone(String phone);

}
