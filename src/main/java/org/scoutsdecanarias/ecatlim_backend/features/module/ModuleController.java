package org.scoutsdecanarias.ecatlim_backend.features.module;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/module")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @GetMapping("/admin/all")
    public List<ModuleDto> getAllModules() {
        log.info("METHOD getAllModules() - Get all modules by {}",
                SecurityContextHolder.getContext().getAuthentication().getName());
        return ModuleDto.fromCollection(this.moduleService.getAll());
    }

    @PostMapping("/admin/create-modules")
    public void addModules(@RequestBody List<ModuleDto> modules) {
        log.info("METHOD addModule() - Adding new modules by {}",
                SecurityContextHolder.getContext().getAuthentication().getName());
        this.moduleService.addModules(modules);
    }
}
