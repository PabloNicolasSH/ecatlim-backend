package org.scoutsdecanarias.ecatlim_backend.event.repository;

import org.scoutsdecanarias.ecatlim_backend.event.entity.EventConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventConfigurationRepository extends JpaRepository<EventConfiguration, Integer> {
}
