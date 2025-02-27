package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.ProductComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductComponentRepository extends JpaRepository<ProductComponent, Long> {

    Optional<ProductComponent> findByProductIdAndComponentId(Long productId, Long componentId);

}
