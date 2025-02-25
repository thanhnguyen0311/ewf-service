package com.danny.ewf_service.repository;
import com.danny.ewf_service.entity.Component;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComponentRepository extends JpaRepository<Component, Long> {
    boolean existsBySku(String sku);

    Optional<Component> findBySku(String sku);

}
