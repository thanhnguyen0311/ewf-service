package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.ProductComponent;
import com.danny.ewf_service.service.impl.ProductServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductComponentRepository extends JpaRepository<ProductComponent, Long> {

    Optional<ProductComponent> findByProductIdAndComponentId(Long productId, Long componentId);


    @Query(value = """
         SELECT
            p.id AS product_id,
            MIN(FLOOR(c.inventory / pc.quantity)) AS inventory
            FROM
            product_components pc
            JOIN components c ON pc.component_id = c.id
            JOIN products p ON pc.product_id = p.id
            WHERE
            c.inventory IS NOT NULL
            GROUP BY
            p.id
            ORDER BY
            MIN(FLOOR(c.inventory / pc.quantity)) ASC""", nativeQuery = true)
    Page<Object[]> calculateProductInventoryByQuantityASC(Pageable pageable);



    @Query(value = """
        SELECT p.id, p.sku
        FROM product_components pc
        JOIN components c ON pc.component_id = c.id
        JOIN products p ON pc.product_id = p.id
        WHERE c.id IN (:ids)
        GROUP BY p.id
        HAVING COUNT(DISTINCT c.sku) = :skuCount
        AND (SELECT COUNT(DISTINCT pc2.component_id)
             FROM product_components pc2
             WHERE pc2.product_id = p.id) = :skuCount
        LIMIT 1 
    """, nativeQuery = true)
    Optional<ProductServiceImpl.ProductProjection> findProductByExactComponents(@Param("ids") List<Long> ids, @Param("skuCount") int skuCount);
}

