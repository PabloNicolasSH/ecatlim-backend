package org.scoutsdecanarias.ecatlim_backend.features.scout_group;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<ScoutGroupDto> addScoutGroup(@Valid @RequestBody ScoutGroupDto scoutGroup) {
        return ResponseEntity.ok(ScoutGroupDto.fromEntity(scoutGroupService.create(scoutGroup)));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}/update")
    public ResponseEntity<ScoutGroupDto> updateScoutGroup(@PathVariable Integer id, @Valid @RequestBody ScoutGroupDto scoutGroup) {
        return ResponseEntity.ok(ScoutGroupDto.fromEntity(scoutGroupService.update(id, scoutGroup)));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteScoutGroup(@PathVariable Integer id) {
        scoutGroupService.delete(id);
        return ResponseEntity.noContent().build();
    }
}