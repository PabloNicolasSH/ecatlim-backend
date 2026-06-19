package org.scoutsdecanarias.ecatlim_backend.features.user_file;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFileRepository extends JpaRepository<UserFile, Integer> {
    default UserFile get(Integer id) {
        return findById(id).orElse(null);
    }
}
