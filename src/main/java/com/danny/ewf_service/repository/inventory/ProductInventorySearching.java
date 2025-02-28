package com.danny.ewf_service.repository.inventory;

import com.danny.ewf_service.entity.ProductComponent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductInventorySearching extends JpaRepository<ProductComponent, Long> {

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
    Page<Object[]> productInventorySearchBySku(Pageable pageable, @Param("sku") String sku);

}
