package org.scoutsdecanarias.ecatlim_backend.service;

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

    public List<User> getUsers() {
        return userRepository.findAll();
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
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        User updatedUser = userRepository.findByEmail(user.getEmail()).orElseThrow(() -> new UsernameNotFoundException(user.getEmail()));

        updatedUser.setName(user.getName());
        updatedUser.setSurname(user.getSurname());
        updatedUser.setEmail(user.getEmail());
        updatedUser.setPassword(user.getPassword());
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
}
