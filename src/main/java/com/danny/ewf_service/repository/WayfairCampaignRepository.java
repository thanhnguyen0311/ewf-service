package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.wayfair.WayfairCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
@Repository
public interface WayfairCampaignRepository extends JpaRepository<WayfairCampaign,Long> {

    Optional<WayfairCampaign> findByCampaignId(String id);

    List<WayfairCampaign> findAllByTypeIsContaining(String type);


}
