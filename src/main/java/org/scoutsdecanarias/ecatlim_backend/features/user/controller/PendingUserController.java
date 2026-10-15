package org.scoutsdecanarias.ecatlim_backend.features.user.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.PendingUserDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.PendingUserFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.service.PendingUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public void createPendingUser(@Valid @RequestBody PendingUserFormDto pendingUserFormDto) {
        log.info("METHOD createPendingUser() - A user with email {} have created a pending user request", pendingUserFormDto.email());
        this.pendingUserService.addPendingUser(pendingUserFormDto);
    }

    @PreAuthorize("hasAuthority('HEAD_OF_EDUCATION')")
    @GetMapping("/head-education/my-scout-group")
    public List<PendingUserDto> getPendingUsersByHeadEducation() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return PendingUserDto.fromCollection(this.pendingUserService.getAllByHeadEducation(email));
    }

    @PreAuthorize("hasAuthority('HEAD_OF_EDUCATION')")
    @PostMapping("/head-education/create-user")
    public void createUserFromPendingUserByHeadEducation(@Validated(PendingUserFormDto.ByEmail.class) @RequestBody PendingUserFormDto pendingUserFormDto) {
        String headEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("METHOD createUserFromPendingUserByHeadEducation() - {} created a user from pending user request by {}", headEmail, pendingUserFormDto.email());
        this.pendingUserService.createUserFromRequestByHeadEducation(headEmail, pendingUserFormDto);
    }

    @PreAuthorize("hasAuthority('HEAD_OF_EDUCATION')")
    @PutMapping("/head-education/delete")
    public void deletePendingUserByHeadEducation(@Validated(PendingUserFormDto.ByEmail.class) @RequestBody PendingUserFormDto pendingUserFormDto) {
        String headEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("METHOD deletePendingUserByHeadEducation() - {} have declined the request of {}", headEmail, pendingUserFormDto.email());
        this.pendingUserService.deletePendingUserByHeadEducation(headEmail, pendingUserFormDto);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/all")
    public List<PendingUserDto> getAllPendingUsers() {
        log.info("METHOD getAllPendingUsers() - {} request all pending users", SecurityContextHolder.getContext().getAuthentication().getName());
        return PendingUserDto.fromCollection(this.pendingUserService.getAllPendingUsers());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/admin/create-user")
    public void createUserFromPendingUser(@Validated(PendingUserFormDto.ByEmail.class) @RequestBody PendingUserFormDto pendingUserFormDto) {
        log.info("METHOD createUserFromPendingUser() - {} created a user from pending user request by {}", SecurityContextHolder.getContext().getAuthentication().getName(), pendingUserFormDto.email());
        this.pendingUserService.createUserFromRequest(pendingUserFormDto);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/admin/delete")
    public void deletePendingUser(@Validated(PendingUserFormDto.ByEmail.class) @RequestBody PendingUserFormDto pendingUserFormDto) {
        log.info("METHOD deletePendingUser() - {} have declined the request of {}", SecurityContextHolder.getContext().getAuthentication().getName(), pendingUserFormDto.email());
        this.pendingUserService.deletePendingUser(pendingUserFormDto);
    }
}