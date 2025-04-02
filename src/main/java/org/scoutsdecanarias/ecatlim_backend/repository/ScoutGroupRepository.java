package org.scoutsdecanarias.ecatlim_backend.repository;

import org.scoutsdecanarias.ecatlim_backend.entity.ScoutGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoutGroupRepository extends JpaRepository<ScoutGroup, Integer> {
    List<ScoutGroup> findAllByProvinceId(int provinceId);
}
