package org.scoutsdecanarias.ecatlim_backend.features.user.service;

import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.TeamMemberDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.UserFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TrainingTeamService {

    public static final Set<Role> TEAM_ROLES = Set.of(Role.TRAINER, Role.EVENT_DIRECTOR, Role.MANAGEMENT);
    public static final Set<Role> ASSIGNABLE_ROLES = Set.of(Role.TRAINER, Role.EVENT_DIRECTOR);

    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<TeamMemberDto> getTeam() {
        return TEAM_ROLES.stream()
                .flatMap(role -> userRepository.findAllByRolesContaining(role).stream())
                .filter(User::isEnabled)
                .distinct()
                .map(TeamMemberDto::fromEntity)
                .sorted(Comparator.comparing((TeamMemberDto m) -> m.surname() == null ? "" : m.surname().toLowerCase())
                        .thenComparing(m -> m.name() == null ? "" : m.name().toLowerCase()))
                .toList();
    }

    @Transactional
    public TeamMemberDto addMember(UserFormDto form) {
        if (form.roles() == null || form.roles().isEmpty() || !ASSIGNABLE_ROLES.containsAll(form.roles())) {
            throw new EcatlimException("Solo se pueden crear personas formadoras o de dirección de eventos", HttpStatus.BAD_REQUEST);
        }
        return TeamMemberDto.fromEntity(userService.addUser(form));
    }

    @Transactional(readOnly = true)
    public List<TeamMemberDto> searchCandidates(String query) {
        String normalized = normalize(query);
        if (normalized.length() < 2) {
            return List.of();
        }
        return userRepository.findAllByRolesContaining(Role.STUDENT).stream()
                .filter(User::isEnabled)
                .filter(user -> !user.getRoles().containsAll(ASSIGNABLE_ROLES))
                .filter(user -> normalize(searchableText(user)).contains(normalized))
                .map(TeamMemberDto::fromEntity)
                .sorted(Comparator.comparing((TeamMemberDto m) -> m.surname() == null ? "" : m.surname().toLowerCase())
                        .thenComparing(m -> m.name() == null ? "" : m.name().toLowerCase()))
                .limit(10)
                .toList();
    }

    @Transactional
    public TeamMemberDto addRole(Integer userId, Role role) {
        if (!ASSIGNABLE_ROLES.contains(role)) {
            throw new EcatlimException("Solo se puede asignar el rol de formador o de dirección de eventos", HttpStatus.BAD_REQUEST);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EcatlimException("Usuario no encontrado", HttpStatus.NOT_FOUND));
        if (!user.isEnabled() || user.getProfile() == null) {
            throw new EcatlimException("Solo se puede asignar el rol a personas activas con perfil", HttpStatus.BAD_REQUEST);
        }
        if (user.getRoles().contains(role)) {
            throw new EcatlimException("Esta persona ya tiene ese rol", HttpStatus.CONFLICT);
        }
        user.getRoles().add(role);
        return TeamMemberDto.fromEntity(userRepository.save(user));
    }

    private String searchableText(User user) {
        return String.join(" ",
                user.getProfile() != null && user.getProfile().getName() != null ? user.getProfile().getName() : "",
                user.getProfile() != null && user.getProfile().getSurname() != null ? user.getProfile().getSurname() : "",
                user.getEmail());
    }

    private String normalize(String text) {
        return text == null ? "" : java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase().trim();
    }

    @Transactional
    public TeamMemberDto removeRole(Integer userId, Role role) {
        if (!ASSIGNABLE_ROLES.contains(role)) {
            throw new EcatlimException("Solo se puede quitar el rol de formador o de dirección de eventos", HttpStatus.BAD_REQUEST);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EcatlimException("Usuario no encontrado", HttpStatus.NOT_FOUND));
        user.getRoles().remove(role);
        return TeamMemberDto.fromEntity(userRepository.save(user));
    }
}
