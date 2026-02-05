package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.payload.projection.ProductComponentDto;
import com.danny.ewf_service.payload.projection.ProductManagementDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.sku = :sku")
    Optional<Product> findProductBySku(@Param("sku") String sku);

    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.components " +
           "LEFT JOIN FETCH p.wholesales " +
           "LEFT JOIN FETCH p.dimension " +
           "LEFT JOIN FETCH p.productDetail " +
           "LEFT JOIN FETCH p.price " +
           "WHERE p.isDeleted = false "+
           "ORDER BY p.createdAt DESC"
    )
    List<Product> findAllProducts();

    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.components " +
           "LEFT JOIN FETCH p.wholesales " +
           "LEFT JOIN FETCH p.price " +
           "LEFT JOIN FETCH p.productDetail " +
           "WHERE p.isDeleted = false " +
           "AND p.id IN :ids " +
           "ORDER BY p.createdAt DESC")
    List<Product> findAllByIds(@Param("ids") List<Long> ids);

    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.components " +
           "LEFT JOIN FETCH p.wholesales " +
           "LEFT JOIN FETCH p.price " +
           "LEFT JOIN FETCH p.productDetail " +
           "WHERE p.isDeleted = false " +
           "AND UPPER(p.sku) IN :skus ")
    List<Product> findAllBySkus(@Param("skus") List<String> skus);

    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.components " +
           "LEFT JOIN FETCH p.wholesales " +
           "LEFT JOIN FETCH p.dimension " +
           "LEFT JOIN FETCH p.productDetail " +
           "WHERE p.wholesales.ewfdirect = true " +
           "AND p.isDeleted = false")
    List<Product> findProductsByWholesalesEwfdirect();

    Optional<Product> findBySku(String sku);

    boolean existsProductByLocalSku(String localSku);

    boolean existsBySku(String sku);

    @Query("SELECT p FROM Product p JOIN FETCH p.price WHERE p.sku IN :skus")
    List<Product> findAllBySkuInIgnoreCase(@Param("skus") List<String> skus);

    @Query("SELECT p FROM Product p WHERE p.asin IS NOT NULL")
    List<Product> findAllByAsinIsNotNull();

    @Query("SELECT p FROM Product p WHERE p.productDetail.collection = :collection")
    List<Product> findAllByProductDetailCollection(@Param("collection") String collection);

    @Query(value = """
        SELECT GROUP_CONCAT(DISTINCT pd.finish ORDER BY pd.finish SEPARATOR ',') AS finishes
        FROM product_details pd
        JOIN products p ON p.detail_id = pd.id
        JOIN product_wholesales pw ON p.wholesales_id = pw.id
        WHERE pd.sub_category = :subCategory
          AND pw.ewfdirect = TRUE
        """, nativeQuery = true)
    String getFinishFilter(@Param("subCategory") String subCategory);


    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.components " +
           "LEFT JOIN FETCH p.wholesales " +
           "LEFT JOIN FETCH p.dimension " +
           "LEFT JOIN FETCH p.productDetail " +
           "LEFT JOIN FETCH p.price " +
           "WHERE p.price.ewfdirect > 0 " +
           "AND p.price.ewfdirect < 500 " +
           "AND p.isDeleted = false  AND p.asin IS NOT NULL\n" +
           "ORDER BY p.createdAt DESC")
    List<Product> findProductsByPriceEwfdirectBetweenZeroAndFiveHundred();


    @Query(value = """
        SELECT GROUP_CONCAT(DISTINCT pd.collection ORDER BY pd.collection SEPARATOR ',') AS collections
        FROM product_details pd
        JOIN products p ON p.detail_id = pd.id
        JOIN product_wholesales pw ON p.wholesales_id = pw.id
        WHERE pd.sub_category = :subCategory
          AND pw.ewfdirect = TRUE
        """, nativeQuery = true)
    String getCollectionFilter(@Param("subCategory") String subCategory);

    @Query(value = """
        SELECT GROUP_CONCAT(DISTINCT pd.style ORDER BY pd.style SEPARATOR ',') AS styles
        FROM product_details pd
        JOIN products p ON p.detail_id = pd.id
        JOIN product_wholesales pw ON p.wholesales_id = pw.id
        WHERE pd.sub_category = :subCategory
          AND pw.ewfdirect = TRUE
        """, nativeQuery = true)
    String getStyleFilter(@Param("subCategory") String subCategory);

    @Query(value = """
        SELECT GROUP_CONCAT(DISTINCT pd.size_shape ORDER BY pd.size_shape SEPARATOR ',') AS sizeShapes
        FROM product_details pd
        JOIN products p ON p.detail_id = pd.id
        JOIN product_wholesales pw ON p.wholesales_id = pw.id
        WHERE pd.sub_category = :subCategory
          AND pw.ewfdirect = TRUE
        """, nativeQuery = true)
    String getSizeShapeFilter(@Param("subCategory") String subCategory);

    @Query(value = """
        SELECT GROUP_CONCAT(DISTINCT pd.chair_type ORDER BY pd.chair_type SEPARATOR ',') AS chairType
        FROM product_details pd
        JOIN products p ON p.detail_id = pd.id
        JOIN product_wholesales pw ON p.wholesales_id = pw.id
        WHERE pd.sub_category = :subCategory
          AND pw.ewfdirect = TRUE
        """, nativeQuery = true)
    String getChairTypeFilter(@Param("subCategory") String subCategory);


    @Query(value = """

        SELECT
           GROUP_CONCAT(DISTINCT pd.collection ORDER BY pd.collection SEPARATOR ',') AS collections
       FROM (
           SELECT *
           FROM products
           ORDER BY id DESC
           LIMIT 1500
        ) AS p
           JOIN product_details pd ON p.detail_id = pd.id
           JOIN product_wholesales pw ON p.wholesales_id = pw.id
           WHERE pw.ewfdirect = TRUE
       """, nativeQuery = true)
    String getCollectionsNewArrivals();


    @Query("""
        SELECT
            p.sku as sku, 
            p.localSku as localSku, 
            p.upc as upc, 
            p.asin as asin, 
            p.shippingMethod as shippingMethod, 
            p.discontinued as discontinued
        FROM Product p
        WHERE p.isDeleted = false
        """)
    List<ProductManagementDto> getAllProduct();

    @Query("""
        SELECT
            p.sku as productSku,  
            c.sku as componentSku,
            pc.quantity as quantity
            FROM Product p
            LEFT JOIN p.components pc
            LEFT JOIN pc.component c
            WHERE p.isDeleted = false
        """)
    List<ProductComponentDto> getAllProductComponents();
}
