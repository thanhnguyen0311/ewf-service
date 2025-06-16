package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.LpnLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LpnLogRepository extends JpaRepository<LpnLog, Long> {


}
