package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p JOIN FETCH p.localProduct WHERE p.sku = :sku")
    Optional<Product> findBySku(@Param("sku") String sku);

    @Query("SELECT p FROM Product p JOIN FETCH p.localProduct WHERE p.category = :category")
    List<Product> findByCategory(@Param("category") String category);

    @Query("SELECT p FROM Product p JOIN FETCH p.localProduct")
    List<Product> findAllProducts();
}
