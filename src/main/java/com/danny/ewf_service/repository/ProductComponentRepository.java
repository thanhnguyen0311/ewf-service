package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.ProductComponent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
            p.id DESC""", nativeQuery = true)
    Page<Object[]> calculateProductInventoryByIdDESC(Pageable pageable);


    @Query(value = """
         SELECT
            p.id AS product_id,
            MIN(FLOOR(c.inventory / pc.quantity)) AS inventory
            FROM
            product_components pc
            JOIN components c ON pc.component_id = c.id
            JOIN products p ON pc.product_id = p.id
            WHERE
            c.inventory IS NOT NULL AND p.sku LIKE  CONCAT('%', :sku, '%')
            GROUP BY
            p.id
            ORDER BY
            p.id DESC""", nativeQuery = true)
    Page<Object[]> calculateProductInventorySearchBySku(Pageable pageable, @Param("sku") String sku);
}

