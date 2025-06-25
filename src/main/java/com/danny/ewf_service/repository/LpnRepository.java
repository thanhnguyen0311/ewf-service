package com.danny.ewf_service.repository;
import com.danny.ewf_service.entity.LPN;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LpnRepository extends JpaRepository<LPN, Long> {
    List<LPN> findAllByOrderByCreatedDateDesc();

    Optional<LPN> findByTagID(String tagID);

    boolean existsLPNByTagID(String tagID);

    List<LPN> findAllByOrderByUpdatedAtDesc();
}
