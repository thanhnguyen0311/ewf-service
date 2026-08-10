package com.danny.ewf_service.wms.repository;

import com.danny.ewf_service.wms.entity.WmsLPN;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WmsLpnRepository extends JpaRepository<WmsLPN, Long> {

    @Query("SELECT l FROM WmsLPN l " +
           "WHERE l.isDeleted = false " +
           "ORDER BY l.updatedAt DESC")
    List<WmsLPN> findAllByOrderByUpdatedAtDesc();
}
