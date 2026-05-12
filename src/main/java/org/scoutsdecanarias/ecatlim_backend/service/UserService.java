package org.scoutsdecanarias.ecatlim_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.scoutsdecanarias.ecatlim_backend.dto.UserFormDto;
import org.scoutsdecanarias.ecatlim_backend.dto.UserMeFormDto;
import org.scoutsdecanarias.ecatlim_backend.entity.User;
import org.scoutsdecanarias.ecatlim_backend.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.exception.UserEmailExistsException;
import org.scoutsdecanarias.ecatlim_backend.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ScoutGroupService scoutGroupService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, ScoutGroupService scoutGroupService, @Lazy PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.scoutGroupService = scoutGroupService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

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

    public User addUser(UserFormDto user) {

        if (userRepository.findByEmail(user.email()).isPresent()) {
            throw new UserEmailExistsException();
        }

        User newUser = new User();
        newUser.setName(user.name());
        newUser.setSurname(user.surname());
        newUser.setEmail(user.email());
        newUser.setPhone(user.phone());
        newUser.setNif(user.nif());
        newUser.setCensus(user.census());
        newUser.setAddress(user.address());
        newUser.setCity(user.city());
        newUser.setCountry(user.country());
        newUser.setRole(user.role());

        if (user.scoutGroupId() != null) {
            newUser.setScoutGroup(scoutGroupService.getScoutGroupById(user.scoutGroupId()));
        }

        PasswordGenerator passwordGenerator = new PasswordGenerator();
        String password = passwordGenerator.generatePassword(12, new CharacterRule(EnglishCharacterData.Alphabetical, 7), new CharacterRule(EnglishCharacterData.Digit, 3));
        newUser.setPassword(passwordEncoder.encode(password));

        emailService.sendWelcomeEmail(newUser.getEmail(), newUser.getName(), newUser.getEmail(), password);

        return userRepository.save(newUser);
    }

    public User updateUser(Integer id, UserFormDto user) {
        User updatedUser = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException(id.toString()));

        if (userRepository.findByEmail(user.email())
                .map(foundUser -> !Objects.equals(foundUser.getId(), id)).orElse(false)) {
            throw new UserEmailExistsException();
        }

        updatedUser.setName(user.name());
        updatedUser.setSurname(user.surname());
        updatedUser.setEmail(user.email());
        updatedUser.setPhone(user.phone());
        updatedUser.setNif(user.nif());
        updatedUser.setCensus(user.census());
        updatedUser.setAddress(user.address());
        updatedUser.setCity(user.city());
        updatedUser.setCountry(user.country());
        updatedUser.setRole(user.role());

        if (user.scoutGroupId() != null) {
            updatedUser.setScoutGroup(scoutGroupService.getScoutGroupById(user.scoutGroupId()));
        }

        return userRepository.save(updatedUser);
    }

    public User updateUserMe(UserMeFormDto userMeFormDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User me = getUserByEmail(email);

        userRepository.findByEmail(userMeFormDto.email()).ifPresent(foundUser -> {
            if(!Objects.equals(foundUser.getId(), me.getId())) {
                throw new UserEmailExistsException();
            }
        });

        me.setName(userMeFormDto.name());
        me.setSurname(userMeFormDto.surname());
        me.setEmail(userMeFormDto.email());
        me.setPhone(userMeFormDto.phone());
        me.setNif(userMeFormDto.nif());
        me.setCensus(userMeFormDto.census());
        me.setAddress(userMeFormDto.address());
        me.setCity(userMeFormDto.city());
        me.setCountry(userMeFormDto.country());

        return userRepository.save(me);
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
