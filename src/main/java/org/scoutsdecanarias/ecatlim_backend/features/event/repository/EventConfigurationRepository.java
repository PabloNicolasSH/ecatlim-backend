package org.scoutsdecanarias.ecatlim_backend.features.event.repository;

import org.scoutsdecanarias.ecatlim_backend.features.event.entity.EventConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventConfigurationRepository extends JpaRepository<EventConfiguration, Integer> {

    @Query("SELECT c.transferBankNumber FROM EventConfiguration c GROUP BY c.transferBankNumber ORDER BY MAX(c.id) DESC")
    List<String> findDistinctTransferBankNumbers();

    @Query("SELECT c.transferCode FROM EventConfiguration c GROUP BY c.transferCode ORDER BY MAX(c.id) DESC")
    List<String> findDistinctTransferCodes();
}
