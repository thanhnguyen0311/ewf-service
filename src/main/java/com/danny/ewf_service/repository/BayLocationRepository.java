package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.BayLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BayLocationRepository extends JpaRepository<BayLocation, Long> {

    List<BayLocation> findAllByIsActiveTrue();

    Optional<BayLocation> findByBayCode(String bayCode);


}
