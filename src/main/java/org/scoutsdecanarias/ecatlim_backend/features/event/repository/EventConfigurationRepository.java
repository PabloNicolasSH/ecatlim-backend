package org.scoutsdecanarias.ecatlim_backend.features.event.repository;

import org.scoutsdecanarias.ecatlim_backend.features.event.entity.EventConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventConfigurationRepository extends JpaRepository<EventConfiguration, Integer> {
}
