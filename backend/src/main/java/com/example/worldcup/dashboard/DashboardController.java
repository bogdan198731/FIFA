package com.example.worldcup.dashboard;

import com.example.worldcup.dashboard.dto.DashboardResponse;
import com.example.worldcup.user.UserPrincipal;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse dashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return dashboardService.getDashboard(principal.getId());
    }
}
