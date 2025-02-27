package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.ProductComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductComponentRepository extends JpaRepository<ProductComponent, Long> {

    Optional<ProductComponent> findByProductIdAndComponentId(Long productId, Long componentId);


    @Query(value = """
                SELECT pc.product_id AS productId, MIN(FLOOR(c.inventory / pc.quantity)) AS totalInventory
                FROM product_components pc
                JOIN components c ON pc.component_id = c.id
                GROUP BY pc.product_id
            """, nativeQuery = true)
    List<Object[]> calculateProductInventory();
}

