package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.BayLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BayLocationRepository extends JpaRepository<BayLocation, Long> {

    @Query(value = """
            SELECT
                                                                                            b.bay_code AS bayCode,
                                                                                            b.default_sku AS defaultSku,
                                                                                            b.zone AS zone,
                                                                                            b.max_pallets AS maxPallets,
                                                                                            COALESCE(COUNT(l.id), 0) AS currentCapacity, -- Current number of assigned LPNs
                                                                                            b.max_pallets - COALESCE(COUNT(l.id), 0) AS availableSpace -- Remaining space
                                                                                        FROM\s
                                                                                            bay_location b
                                                                                        LEFT JOIN\s
                                                                                            lpn l\s
                                                                                        ON\s
                                                                                            b.id = l.bay_id
                                                                                        WHERE\s
                                                                                            b.is_active = true
                                                                                        GROUP BY\s
                                                                                            b.id, b.bay_code, b.zone, b.max_pallets
                                                                                        ORDER BY\s
                                                                                            b.bay_code ASC;
            """,
            nativeQuery = true)
    List<Object[]> findAllBayByIsActiveTrue();

    Optional<BayLocation> findByBayCode(String bayCode);


}
