package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.BayLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BayLocationRepository extends JpaRepository<BayLocation, Long> {
}
