package com.danny.ewf_service.repository.Wayfair;

import com.danny.ewf_service.entity.wayfair.WayfairCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface WayfairCategoryRepository  extends JpaRepository<WayfairCategory,Long> {
    Optional<WayfairCategory> findByTitle(String title);
}
