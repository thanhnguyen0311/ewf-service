package com.danny.ewf_service.repository;
import com.danny.ewf_service.entity.LPN;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface LpnRepository extends JpaRepository<LPN, Long> {
    List<LPN> findAllByOrderByCreatedDateDesc();

    Optional<LPN> findByTagID(String tagID);

    boolean existsLPNByTagID(String tagID);

    @Query("SELECT l FROM LPN l " +
           "LEFT JOIN FETCH l.component c " +
           "LEFT JOIN FETCH l.bayLocation b " +
           "WHERE l.isDeleted = false " +
           "ORDER BY l.updatedAt DESC")
    List<LPN> findAllByOrderByUpdatedAtDesc();

    @Query("SELECT l FROM LPN l " +
           "LEFT JOIN FETCH l.component c " +
           "LEFT JOIN FETCH l.bayLocation b " +
           "WHERE l.tagID IN :tagIDs AND c.sku like :sku AND l.isDeleted = false " +
           "ORDER BY l.updatedAt DESC")
    List<LPN> findByTagIDsOrderByUpdatedAtDesc(List<String> tagIDs, String sku);

}
