package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.LooseInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LooseInventoryRepository  extends JpaRepository<LooseInventory, Long> {
}
