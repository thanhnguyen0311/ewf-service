package com.danny.ewf_service.repository;
import com.danny.ewf_service.entity.Component;
import com.danny.ewf_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComponentRepository extends JpaRepository<Component, Long> {
    boolean existsBySku(String sku);

    Optional<Component> findBySku(String sku);

    @Query("SELECT c FROM Component c JOIN FETCH c.report")
    List<Component> findAllComponents();

    @Query("SELECT c FROM Component c JOIN FETCH c.report WHERE c.id = :id")
    Component findComponentById(@Param("id") Long id);
}
