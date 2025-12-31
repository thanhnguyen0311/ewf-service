package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.wayfair.WayfairParentSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WayfairParentSkuRepository extends JpaRepository<WayfairParentSku,Long> {

    Optional<WayfairParentSku> findByParentSku(String sku);


}
