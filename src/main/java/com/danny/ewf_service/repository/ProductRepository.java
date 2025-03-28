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

    @Query("SELECT p FROM Product p JOIN FETCH p.price JOIN FETCH p.productDetail JOIN FETCH p.wholesales JOIN FETCH p.productComponents")
    List<Product> findAllProducts();

    Optional<Product> findBySkuIgnoreCase(String sku);

    Optional<Product> findBySku(String sku);

    List<Product> findAllByIdIn(List<Long> ids);

    boolean existsProductByLocalSku(String localSku);

    boolean existsBySku(String sku);

    @Query("SELECT p FROM Product p JOIN FETCH p.price WHERE p.sku IN :skus")
    List<Product> findAllBySkuInIgnoreCase(@Param("skus") List<String> skus);
}
