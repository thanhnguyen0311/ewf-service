package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.LpnLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LpnLogRepository extends JpaRepository<LpnLog, Long> {

    @Query("SELECT l.id FROM LpnLog l ORDER BY l.logDate DESC")
    Page<Long> findAllLpnLogIds(Pageable pageable);


    @Query("SELECT l FROM LpnLog l " +
           "JOIN FETCH l.user u " +
           "JOIN FETCH l.lpn lpn " +
           "JOIN FETCH lpn.component " +
           "WHERE l.id IN :ids " +
           "ORDER BY l.logDate DESC")
    List<LpnLog> findAllByOrderByLogDateDesc(List<Long> ids);

}
