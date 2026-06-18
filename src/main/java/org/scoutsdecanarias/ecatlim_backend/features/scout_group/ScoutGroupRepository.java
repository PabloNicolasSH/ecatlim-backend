package org.scoutsdecanarias.ecatlim_backend.features.scout_group;

import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoutGroupRepository extends JpaRepository<ScoutGroup, Integer> {
    List<ScoutGroup> findAllByProvinceId(int provinceId);

    @Query("SELECT sg.headOfEducation.user FROM ScoutGroup sg WHERE sg.headOfEducation IS NOT NULL")
    List<User> findAllHeadsOfEducation();
}
