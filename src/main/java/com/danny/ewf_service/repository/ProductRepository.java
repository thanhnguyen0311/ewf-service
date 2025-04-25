package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.product.Product;
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
           "LEFT JOIN FETCH p.productDetail " +
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


    @Query("SELECT p FROM Product p JOIN p.wholesales w WHERE w.ewfdirect = true")
    List<Product> findProductsByWholesalesEwfdirect();

    Optional<Product> findBySku(String sku);

    boolean existsProductByLocalSku(String localSku);

    boolean existsBySku(String sku);

    @Query("SELECT p FROM Product p JOIN FETCH p.price WHERE p.sku IN :skus")
    List<Product> findAllBySkuInIgnoreCase(@Param("skus") List<String> skus);

    @Query("SELECT p FROM Product p WHERE p.asin IS NOT NULL")
    List<Product> findAllByAsinIsNotNull();
}
