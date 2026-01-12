package com.danny.ewf_service.repository.Wayfair;

import com.danny.ewf_service.entity.wayfair.WayfairBiddingLogic;
import com.danny.ewf_service.entity.wayfair.WayfairCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WayfairBiddingLogicRepository extends JpaRepository<WayfairBiddingLogic,Long> {

    Optional<WayfairBiddingLogic> findByCategory(WayfairCategory category);

}
