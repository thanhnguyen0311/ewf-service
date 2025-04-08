package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.product.ProductComponent;
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
          SELECT p.*, MIN(FLOOR(c.inventory / pc.quantity)) AS inventory
             FROM product_components pc
             JOIN components c ON pc.component_id = c.id
             JOIN products p ON pc.product_id = p.id
             WHERE c.inventory IS NOT NULL
             GROUP BY p.id
             ORDER BY inventory ASC
         """, nativeQuery = true)
    List<Object[]> calculateAllProductInventoryByQuantityASC();

    @Query(value = """
             SELECT p.sku, p.title, MIN(FLOOR(c.inventory / pc.quantity)) AS inventory
                FROM product_components pc
                JOIN components c ON pc.component_id = c.id
                JOIN products p ON pc.product_id = p.id
                JOIN product_wholesales w ON p.wholesales_id = w.id  
                WHERE c.inventory IS NOT NULL
                    AND w.ewfdirect = TRUE
                GROUP BY p.id
            """, nativeQuery = true)
    List<Object[]> calculateListProductInventoryShopifyEWFDirectByQuantityASC();

    @Query(value = """
            SELECT c.sku 
            FROM product_components pc 
            JOIN components c ON pc.component_id = c.id 
            JOIN products p ON pc.product_id = p.id 
            WHERE p.id = :id AND c.type = 'Single'""",
            nativeQuery = true)
    List<String> findSingleProductsByProductSku(@Param("id") Long id);


    @Query(value = """
        SELECT
            MIN(FLOOR(c.inventory / pc.quantity)) AS inventory
        FROM
            product_components pc
        JOIN
            components c ON pc.component_id = c.id
        JOIN
            products p ON pc.product_id = p.id
        WHERE
            p.id = :productId
            AND c.inventory IS NOT NULL
        GROUP BY
            p.id
        """, nativeQuery = true)
    Long countByProductId(@Param("productId") Long productId);


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
    Optional<ProductServiceImpl.ProductMergedProjection> findProductByExactComponents(@Param("ids") List<Long> ids, @Param("skuCount") int skuCount);


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


    @Query(value = """
               SELECT
                        p.id AS id,
                        p.sku AS sku,
                        MIN(FLOOR(c.inventory / pc.quantity)) AS quantity,
                        p.asin as asin,
                        p.upc as upc,
                        p.discontinued AS discontinued,
                        pw.ewfdirect AS ewfdirect,
                        pw.amazon AS amazon,
                        pw.cymax as cymax,
                        pw.overstock as overstock,
                        pw.wayfair as wayfair,
                        p.local_sku as local_sku
                    FROM
                        product_components pc
                    INNER JOIN
                        components c ON pc.component_id = c.id
                    INNER JOIN
                        products p ON pc.product_id = p.id
                    INNER JOIN
                        product_wholesales pw ON p.wholesales_id = pw.id
                    WHERE p.is_deleted = False
                    GROUP BY
                        p.id, p.sku, p.discontinued
                    ORDER BY
                        p.id DESC
            """, nativeQuery = true)
    List<Object[]> productInventoryAll();

}

