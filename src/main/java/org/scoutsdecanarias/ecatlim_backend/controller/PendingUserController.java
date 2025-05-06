package org.scoutsdecanarias.ecatlim_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.dto.PendingUserFormDto;
import org.scoutsdecanarias.ecatlim_backend.service.PendingUserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
