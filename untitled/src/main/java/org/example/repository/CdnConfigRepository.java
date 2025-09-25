package org.example.repository;

import org.example.entity.CdnConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CdnConfigRepository extends JpaRepository<CdnConfig, Long> {

    List<CdnConfig> findByStatus(CdnConfig.CdnStatus status);

    long countByStatus(CdnConfig.CdnStatus status);

    List<CdnConfig> findByProviderName(String providerName);
}