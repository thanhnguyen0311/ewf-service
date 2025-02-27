package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.LocalProduct;
import com.danny.ewf_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocalRepository extends JpaRepository<LocalProduct, Long> {
    @Query("SELECT lp FROM LocalProduct lp LEFT JOIN FETCH lp.product")
    List<LocalProduct> findAllWithProducts();

    Optional<LocalProduct> findByLocalSku(String sku);

}
