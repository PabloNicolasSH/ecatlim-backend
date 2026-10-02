package org.scoutsdecanarias.ecatlim_backend.features.scout_group;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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