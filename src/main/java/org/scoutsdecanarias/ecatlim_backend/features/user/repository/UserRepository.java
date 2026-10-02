package org.scoutsdecanarias.ecatlim_backend.features.user.repository;

import org.scoutsdecanarias.ecatlim_backend.features.scout_group.ScoutGroup;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    List<User> findAllByEnabled(boolean enabled);
    List<User> findAllByRolesContaining(Role role);

    Integer countUsersByEnabled(boolean enabled);

    Integer countUsersByEnabledAndRolesContaining(boolean enabled, Role role);

    List<User> findAllByRolesContainingAndProfile_ScoutGroup(Set<Role> roles, ScoutGroup profileScoutGroup);
}
