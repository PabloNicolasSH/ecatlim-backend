package org.scoutsdecanarias.ecatlim_backend.admin_dashboard;

import org.scoutsdecanarias.ecatlim_backend.admin_dashboard.dto.DashboardDto;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public ResponseEntity<DashboardDto> getDashboardData() {
        DashboardDto data = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(data);
    }
}
