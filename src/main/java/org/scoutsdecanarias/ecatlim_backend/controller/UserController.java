package org.scoutsdecanarias.ecatlim_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.dto.UserProfileDto;
import org.scoutsdecanarias.ecatlim_backend.entity.User;
import org.scoutsdecanarias.ecatlim_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collections;
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

    @GetMapping("/admin/all")
    public List<UserProfileDto> getUsers() {
        return UserProfileDto.fromCollection(userService.getUsers());
    }

    @PostMapping("/admin/add")
    public UserProfileDto addUser(@RequestBody User user) {
        log.info("Adding user: {}", user);
        return UserProfileDto.fromEntity(userService.addUser(user));
    }
}
