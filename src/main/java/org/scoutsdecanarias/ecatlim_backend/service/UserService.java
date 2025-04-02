package org.scoutsdecanarias.ecatlim_backend.service;

import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.scoutsdecanarias.ecatlim_backend.entity.User;
import org.scoutsdecanarias.ecatlim_backend.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getActiveUsers() {
        return userRepository.findAllByEnabled(true);
    }

    public List<User> getInactiveUsers() {
        return userRepository.findAllByEnabled(false);
    }

    public List<User> getUsersByRole(Role role) {
        List<User> users = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            if (user.getRole() == role) {
                users.add(user);
            }
        }
        return users;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
    }

    public User getUserByNif(String nif) {
        return userRepository.findByNif(nif).orElseThrow(() -> new UsernameNotFoundException(nif));
    }

    public User addUser(User user) {
        PasswordGenerator passwordGenerator = new PasswordGenerator();
        user.setPassword(passwordGenerator
                .generatePassword(12, new CharacterRule(EnglishCharacterData.Alphabetical, 7), new CharacterRule(EnglishCharacterData.Digit, 3)));
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException();
        }
        return userRepository.save(user);
    }

    public User updateUser(Integer id, User user) {
        User updatedUser = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException(id.toString()));

        updatedUser.setName(user.getName());
        updatedUser.setSurname(user.getSurname());
        updatedUser.setEmail(user.getEmail());
        updatedUser.setNif(user.getNif());
        updatedUser.setRole(user.getRole());
        updatedUser.setCountry(user.getCountry());
        updatedUser.setCensus(user.getCensus());
        updatedUser.setPhone(user.getPhone());

        return userRepository.save(updatedUser);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), user.isEnabled(),
                    true, true, true, this.buildAuthorities(user.getRole()));
        }
        return null;
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
