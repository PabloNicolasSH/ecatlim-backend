package org.scoutsdecanarias.ecatlim_backend.core.auth.mfa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MfaServiceTest {

    @Mock UserRepository userRepository;
    @Mock MfaRecoveryCodeRepository recoveryCodeRepository;

    private final SecretCipher cipher = new SecretCipher("test-key-material");
    private MfaService mfaService;
    private User user;

    @BeforeEach
    void setUp() {
        mfaService = new MfaService(userRepository, recoveryCodeRepository, cipher, "ECATLIM");
        user = new User();
        user.setId(7);
        user.setEmail("head@test.com");
        user.getRoles().add(Role.HEAD_OF_EDUCATION);
    }

    private String currentCode() {
        return Totp.codeAt(cipher.decrypt(user.getTotpSecret()), Totp.stepAt(System.currentTimeMillis()));
    }

    private void enable() {
        mfaService.beginSetup(user);
        mfaService.confirmSetup(user, currentCode());
    }

    @Test
    void requiredForTheSensitiveRolesOnly() {
        for (Role role : List.of(Role.ADMIN, Role.HEAD_OF_EDUCATION, Role.MANAGER_DIRECTOR, Role.MANAGEMENT)) {
            User sensitive = new User();
            sensitive.getRoles().add(role);
            assertThat(MfaService.isRequiredFor(sensitive)).as(role.name()).isTrue();
        }
        User student = new User();
        student.getRoles().addAll(List.of(Role.STUDENT, Role.TRAINER, Role.EVENT_DIRECTOR));
        assertThat(MfaService.isRequiredFor(student)).isFalse();
    }

    @Test
    void setupStoresAnEncryptedSecretThatOnlyCountsOnceConfirmed() {
        MfaService.Setup setup = mfaService.beginSetup(user);

        assertThat(user.getTotpSecret()).isNotEqualTo(setup.secret());
        assertThat(cipher.decrypt(user.getTotpSecret())).isEqualTo(setup.secret());
        assertThat(setup.otpauthUri()).contains(setup.secret());
        assertThat(user.isTotpEnabled()).isFalse();
    }

    @Test
    void confirmingWithAValidCodeEnablesItAndReturnsRecoveryCodes() {
        mfaService.beginSetup(user);

        List<String> codes = mfaService.confirmSetup(user, currentCode());

        assertThat(user.isTotpEnabled()).isTrue();
        assertThat(codes).hasSize(8).allMatch(code -> code.matches("[A-Z2-9]{5}-[A-Z2-9]{5}"));
        verify(recoveryCodeRepository, times(8)).save(any(MfaRecoveryCode.class));
    }

    @Test
    void confirmingWithAWrongCodeDoesNotEnableIt() {
        mfaService.beginSetup(user);

        assertThatThrownBy(() -> mfaService.confirmSetup(user, "000000"))
                .isInstanceOf(EcatlimException.class)
                .extracting("status").isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(user.isTotpEnabled()).isFalse();
    }

    @Test
    void aCodeCannotBeUsedTwice() {
        enable();
        long nextStep = Totp.stepAt(System.currentTimeMillis()) + 1;
        String next = Totp.codeAt(cipher.decrypt(user.getTotpSecret()), nextStep);

        mfaService.verifyLogin(user, next);

        assertThatThrownBy(() -> mfaService.verifyLogin(user, next))
                .isInstanceOf(EcatlimException.class)
                .extracting("status").isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void aRecoveryCodeWorksOnceAndIsAcceptedInLowerCase() {
        mfaService.beginSetup(user);
        List<String> codes = mfaService.confirmSetup(user, currentCode());
        ArgumentCaptor<MfaRecoveryCode> saved = ArgumentCaptor.forClass(MfaRecoveryCode.class);
        verify(recoveryCodeRepository, times(8)).save(saved.capture());
        MfaRecoveryCode first = saved.getAllValues().get(0);
        when(recoveryCodeRepository.findByUserIdAndCodeHashAndUsedAtIsNull(7, first.getCodeHash())).thenReturn(Optional.of(first));

        mfaService.verifyLogin(user, codes.get(0).toLowerCase());

        assertThat(first.getUsedAt()).isNotNull();
    }

    @Test
    void anUnknownRecoveryCodeIsRejected() {
        enable();

        assertThatThrownBy(() -> mfaService.verifyLogin(user, "AAAAA-BBBBB"))
                .isInstanceOf(EcatlimException.class)
                .extracting("status").isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void locksAfterFiveWrongCodes() {
        enable();

        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> mfaService.verifyLogin(user, "000000"))
                    .isInstanceOf(EcatlimException.class)
                    .extracting("status").isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        assertThatThrownBy(() -> mfaService.verifyLogin(user, currentCode()))
                .isInstanceOf(EcatlimException.class)
                .extracting("status").isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void resetRemovesTheSecretAndTheRecoveryCodes() {
        enable();

        mfaService.reset(user);

        assertThat(user.isTotpEnabled()).isFalse();
        assertThat(user.getTotpSecret()).isNull();
        verify(recoveryCodeRepository, atLeastOnce()).deleteAllForUser(7);
    }
}
