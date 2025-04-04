package org.scoutsdecanarias.ecatlim_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.dto.UserFormDto;
import org.scoutsdecanarias.ecatlim_backend.dto.UserProfileDto;
import org.scoutsdecanarias.ecatlim_backend.service.UserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserProfileDto getUserInfo(Principal principal) {
        return UserProfileDto.fromEntity(userService.getUserByEmail(principal.getName()));
    }

    @GetMapping("/admin/allActives")
    public List<UserProfileDto> getActiveUsers() {
        return UserProfileDto.fromCollection(userService.getActiveUsers());
    }

    @GetMapping("/admin/allInactives")
    public List<UserProfileDto> getInactiveUsers() {
        return UserProfileDto.fromCollection(userService.getInactiveUsers());
    }

    @PostMapping("/admin/add")
    public UserProfileDto addUser(@RequestBody UserFormDto user) {
        log.info("Adding user: {}", user);
        return UserProfileDto.fromEntity(userService.addUser(user));
    }

    @PutMapping("/admin/edit/{id}")
    public UserProfileDto editUser(@PathVariable Integer id, @RequestBody UserFormDto user) {
        log.info("Updating user: {}", user);
        return UserProfileDto.fromEntity(userService.updateUser(id, user));
    }

    @PutMapping("/admin/activate/{id}")
    public UserProfileDto activateUser(@PathVariable Integer id) {
        log.info("Activating user: {}", id);
        return UserProfileDto.fromEntity(userService.activateUser(id));
    }

    @DeleteMapping("/admin/deactivate/{id}")
    public UserProfileDto deactivateUser(@PathVariable Integer id) {
        log.info("Deactivating user: {}", id);
        return UserProfileDto.fromEntity(userService.deactivateUser(id));
    }

}
