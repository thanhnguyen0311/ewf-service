package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.LocalProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalRepository extends JpaRepository<LocalProduct, Long> {
    @Query("SELECT lp FROM LocalProduct lp LEFT JOIN FETCH lp.product")
    List<LocalProduct> findAllWithProducts();

}
