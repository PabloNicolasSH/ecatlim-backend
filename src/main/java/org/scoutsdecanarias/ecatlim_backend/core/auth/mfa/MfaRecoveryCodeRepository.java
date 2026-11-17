package org.scoutsdecanarias.ecatlim_backend.core.auth.mfa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MfaRecoveryCodeRepository extends JpaRepository<MfaRecoveryCode, Integer> {

    Optional<MfaRecoveryCode> findByUserIdAndCodeHashAndUsedAtIsNull(Integer userId, String codeHash);

    long countByUserIdAndUsedAtIsNull(Integer userId);

    @Modifying
    @Query("DELETE FROM MfaRecoveryCode c WHERE c.user.id = :userId")
    void deleteAllForUser(@Param("userId") Integer userId);
}
