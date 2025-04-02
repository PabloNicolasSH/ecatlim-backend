package org.scoutsdecanarias.ecatlim_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.dto.ScoutGroupDto;
import org.scoutsdecanarias.ecatlim_backend.service.ScoutGroupService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/scout-group")
public class ScoutGroupController {

    private final ScoutGroupService scoutGroupService;

    public ScoutGroupController(ScoutGroupService scoutGroupService) {
        this.scoutGroupService = scoutGroupService;
    }

    @GetMapping("/all")
    public List<ScoutGroupDto> getScoutGroups() {
        return ScoutGroupDto.fromCollection(scoutGroupService.getScoutGroups());
    }
}