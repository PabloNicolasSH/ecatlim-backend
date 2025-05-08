package org.scoutsdecanarias.ecatlim_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.dto.PendingUserDto;
import org.scoutsdecanarias.ecatlim_backend.dto.PendingUserFormDto;
import org.scoutsdecanarias.ecatlim_backend.service.PendingUserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/pending-user")
public class PendingUserController {

    private final PendingUserService pendingUserService;

    public PendingUserController(PendingUserService pendingUserService) {
        this.pendingUserService = pendingUserService;
    }

    @PostMapping("/request")
    public void createPendingUser(@RequestBody PendingUserFormDto pendingUserFormDto) {
        log.info("METHOD createPendingUser() - A user with email {} have created a pending user request", pendingUserFormDto.email());
        this.pendingUserService.addPendingUser(pendingUserFormDto);
    }

    @GetMapping("/admin/all")
    public List<PendingUserDto> getAllPendingUsers() {
        log.info("METHOD getAllPendingUsers() - {} request all pending users", SecurityContextHolder.getContext().getAuthentication().getName());
        return PendingUserDto.fromCollection(this.pendingUserService.getAllPendingUsers());
    }

    @PostMapping("/admin/create-user")
    public void createUserFromPendingUser(@RequestBody PendingUserFormDto pendingUserFormDto) {
        log.info("METHOD createUserFromPendingUser() - {} created a user from pending user request by {}", SecurityContextHolder.getContext().getAuthentication().getName(), pendingUserFormDto.email());
        this.pendingUserService.createUserFromRequest(pendingUserFormDto);
    }

    @PutMapping("/admin/delete")
    public void deletePendingUser(@RequestBody PendingUserFormDto pendingUserFormDto) {
        log.info("METHOD deletePendingUser() - {} have declined the request of {}", SecurityContextHolder.getContext().getAuthentication().getName(), pendingUserFormDto.email());
        this.pendingUserService.deletePendingUser(pendingUserFormDto);
    }
}