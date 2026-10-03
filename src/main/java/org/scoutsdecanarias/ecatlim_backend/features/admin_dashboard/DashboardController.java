package org.scoutsdecanarias.ecatlim_backend.features.admin_dashboard;

import org.scoutsdecanarias.ecatlim_backend.features.admin_dashboard.dto.DashboardDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @PreAuthorize("hasAnyAuthority('MANAGER_DIRECTOR', 'MANAGEMENT', 'EVENT_DIRECTOR', 'TRAINER')")
    @GetMapping
    public ResponseEntity<DashboardDto> getDashboardData() {
        DashboardDto data = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(data);
    }
}
