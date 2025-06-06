package com.danny.ewf_service.repository;
import com.danny.ewf_service.entity.LPN;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LpnRepository extends JpaRepository<LPN, Long> {
}
