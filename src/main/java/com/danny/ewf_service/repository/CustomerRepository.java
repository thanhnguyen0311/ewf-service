package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByPhone(String phone); //

    List<Customer> findByPhone(String phone);

    @Query("SELECT c FROM Customer c WHERE c.phone LIKE CONCAT('%', :partialPhone, '%')")
    List<Customer> findByPartialPhoneNumber(@Param("partialPhone") String partialPhone);
}
