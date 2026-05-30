package org.scoutsdecanarias.ecatlim_backend.admin_dashboard;

import org.scoutsdecanarias.ecatlim_backend.admin_dashboard.dto.DashboardDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class DashboardService {
    public DashboardDto getDashboardSummary() {
        return new DashboardDto(
                1240,
                12,
                "Curso BFCS-14",
                LocalDateTime.of(2026,6, 10, 20, 0),
                7,
                "Campismo para muchachos",
                93,
                "80%",
                null
        );
    }
}
