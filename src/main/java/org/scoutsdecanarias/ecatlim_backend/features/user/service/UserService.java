package org.scoutsdecanarias.ecatlim_backend.features.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.scoutsdecanarias.ecatlim_backend.core.exception.UserEmailExistsException;
import org.scoutsdecanarias.ecatlim_backend.features.scout_group.ScoutGroupService;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.UserFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.UserMeFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.UserProfile;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.scoutsdecanarias.ecatlim_backend.shared.email.EmailService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ScoutGroupService scoutGroupService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public List<User> getActiveUsers() {
        return userRepository.findAllByEnabled(true);
    }

    public List<User> getInactiveUsers() {
        return userRepository.findAllByEnabled(false);
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findAllByRole(role);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
    }

    @Transactional
    public User addUser(UserFormDto user) {

        if (userRepository.findByEmail(user.email()).isPresent()) {
            throw new UserEmailExistsException();
        }

        User newUser = new User();
        newUser.setEmail(user.email());
        newUser.setRole(user.role());

        PasswordGenerator passwordGenerator = new PasswordGenerator();
        String password = passwordGenerator.generatePassword(12,
                new CharacterRule(EnglishCharacterData.Alphabetical, 7),
                new CharacterRule(EnglishCharacterData.Digit, 3));
        newUser.setPassword(passwordEncoder.encode(password));

        String emailNameParam = null;

        if (user.role() != Role.ADMIN) {
            UserProfile profile = new UserProfile();
            profile.setUser(newUser);
            profile.setName(user.name());
            profile.setSurname(user.surname());
            profile.setNif(user.nif());
            profile.setAddress(user.address());
            profile.setCountry(user.country());
            profile.setCity(user.city());
            profile.setPhone(user.phone());
            profile.setCensus(user.census());

            if (user.scoutGroupId() != null) {
                profile.setScoutGroup(scoutGroupService.getScoutGroupById(user.scoutGroupId()));
            }

            newUser.setProfile(profile);
            emailNameParam = profile.getName();
        }

        emailService.sendWelcomeEmail(newUser.getEmail(), emailNameParam, password);

        return userRepository.save(newUser);
    }

    @Transactional
    public User updateUser(Integer id, UserFormDto user) {
        User updatedUser = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException(id.toString()));

        if (userRepository.findByEmail(user.email())
                .map(foundUser -> !Objects.equals(foundUser.getId(), id)).orElse(false)) {
            throw new UserEmailExistsException();
        }

        updatedUser.setEmail(user.email());
        updatedUser.setRole(user.role());

        if (user.role() != Role.ADMIN) {
            UserProfile profile = updatedUser.getProfile() != null ? updatedUser.getProfile() : new UserProfile();

            profile.setUser(updatedUser);
            profile.setName(user.name());
            profile.setSurname(user.surname());
            profile.setNif(user.nif());
            profile.setAddress(user.address());
            profile.setCountry(user.country());
            profile.setCity(user.city());
            profile.setPhone(user.phone());
            profile.setCensus(user.census());

            if (user.scoutGroupId() != null) {
                profile.setScoutGroup(scoutGroupService.getScoutGroupById(user.scoutGroupId()));
            } else {
                profile.setScoutGroup(null);
            }

            updatedUser.setProfile(profile);
        } else {
            updatedUser.setProfile(null);
        }

        return userRepository.save(updatedUser);
    }

    @Transactional
    public User updateUserMe(UserMeFormDto userMeFormDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User me = getUserByEmail(email);

        userRepository.findByEmail(userMeFormDto.email()).ifPresent(foundUser -> {
            if(!Objects.equals(foundUser.getId(), me.getId())) {
                throw new UserEmailExistsException();
            }
        });

        me.setEmail(userMeFormDto.email());

        if (me.getRole() != Role.ADMIN) {
            UserProfile profile = me.getProfile() != null ? me.getProfile() : new UserProfile();

            profile.setUser(me);
            profile.setName(userMeFormDto.name());
            profile.setSurname(userMeFormDto.surname());
            profile.setNif(userMeFormDto.nif());
            profile.setAddress(userMeFormDto.address());
            profile.setCountry(userMeFormDto.country());
            profile.setCity(userMeFormDto.city());
            profile.setPhone(userMeFormDto.phone());
            profile.setCensus(userMeFormDto.census());

            me.setProfile(profile);
        }

        return userRepository.save(me);
    }

    @Transactional
    public void updateMyAvatar(User user, String avatarUrl) {
        UserProfile profile = user.getProfile() != null ? user.getProfile() : new UserProfile();
        profile.setProfilePictureUrl(avatarUrl);
        user.setProfile(profile);
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = getUserByEmail(email);
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), user.isEnabled(),
                true, true, true, this.buildAuthorities(user.getRole()));
    }

    private List<GrantedAuthority> buildAuthorities(Role userRole) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(userRole.name()));
        return new ArrayList<>(authorities);
    }

    public User activateUser(Integer id) {
        User userToActive = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException(id.toString()));
        userToActive.setEnabled(true);
        return userRepository.save(userToActive);
    }

    public User deactivateUser(Integer id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException(id.toString()));
        user.setEnabled(false);
        return userRepository.save(user);
    }
}