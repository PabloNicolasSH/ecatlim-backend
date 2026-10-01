package org.scoutsdecanarias.ecatlim_backend.features.user.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.UserFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.UserMeFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.UserProfileDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.features.user.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserProfileDto getUserInfo() {
        log.info("METHOD getUserInfo() - Get user info for principal: {}", SecurityContextHolder.getContext().getAuthentication().getName());
        return UserProfileDto.fromEntity(userService.getUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()));
    }

    @PutMapping("/update/me")
    public UserProfileDto updateMyUserInfo(@RequestBody UserMeFormDto userMeFormDto) {
        log.info("METHOD updateMyUserInfo() - Update user info for principal: {}", SecurityContextHolder.getContext().getAuthentication().getName());
        return UserProfileDto.fromEntity(userService.updateUserMe(userMeFormDto));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/allActives")
    public List<UserProfileDto> getActiveUsers() {
        log.info("METHOD getActiveUsers() - Get active users by: {}", SecurityContextHolder.getContext().getAuthentication().getName());
        return UserProfileDto.fromCollection(userService.getActiveUsers());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/all/{role}")
    public List<UserProfileDto> getUsersByRole(@PathVariable String role) {
        log.info("METHOD getUsersByRole() - Get users by role: {}", role);
        Role r = Role.valueOf(role);
        return UserProfileDto.fromCollection(userService.getUsersByRole(r));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/allInactives")
    public List<UserProfileDto> getInactiveUsers() {
        log.info("METHOD getInactiveUsers() - Get inactive users by: {}", SecurityContextHolder.getContext().getAuthentication().getName());
        return UserProfileDto.fromCollection(userService.getInactiveUsers());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/admin/add")
    public UserProfileDto addUser(@RequestBody UserFormDto user) {
        log.info("METHOD addUser - Adding user: {}, by: {}", user, SecurityContextHolder.getContext().getAuthentication().getName());
        return UserProfileDto.fromEntity(userService.addUser(user));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/admin/edit/{id}")
    public UserProfileDto editUser(@PathVariable Integer id, @RequestBody UserFormDto user) {
        log.info("METHOD editUser() - Updating user: {}, by: {}", user, SecurityContextHolder.getContext().getAuthentication().getName());
        return UserProfileDto.fromEntity(userService.updateUser(id, user));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/admin/activate/{id}")
    public UserProfileDto activateUser(@PathVariable Integer id) {
        log.info("METHOD activateUser - Activating user: {} by: {}", id, SecurityContextHolder.getContext().getAuthentication().getName());
        return UserProfileDto.fromEntity(userService.activateUser(id));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/admin/deactivate/{id}")
    public UserProfileDto deactivateUser(@PathVariable Integer id) {
        log.info("METHOD deactivateUser - Deactivating user: {}, by: {}", id, SecurityContextHolder.getContext().getAuthentication().getName());
        return UserProfileDto.fromEntity(userService.deactivateUser(id));
    }

}
