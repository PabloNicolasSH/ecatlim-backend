package org.scoutsdecanarias.ecatlim_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.dto.ScoutGroupDto;
import org.scoutsdecanarias.ecatlim_backend.service.ScoutGroupService;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/add")
    public ResponseEntity<ScoutGroupDto> addScoutGroup(@RequestBody ScoutGroupDto scoutGroup) {
        return ResponseEntity.ok(ScoutGroupDto.fromEntity(scoutGroupService.create(scoutGroup)));
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<ScoutGroupDto> updateScoutGroup(@PathVariable Integer id, @RequestBody ScoutGroupDto scoutGroup) {
        return ResponseEntity.ok(ScoutGroupDto.fromEntity(scoutGroupService.update(id, scoutGroup)));
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteScoutGroup(@PathVariable Integer id) {
        scoutGroupService.delete(id);
        return ResponseEntity.noContent().build();
    }
}