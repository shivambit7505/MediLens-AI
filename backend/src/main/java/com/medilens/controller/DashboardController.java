package com.medilens.controller;

import com.medilens.dto.trend.PatientDashboardSummaryDto;
import com.medilens.exception.UnauthorizedException;
import com.medilens.security.UserPrincipal;
import com.medilens.service.BiomarkerTrendService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final BiomarkerTrendService biomarkerTrendService;

    public DashboardController(BiomarkerTrendService biomarkerTrendService) {
        this.biomarkerTrendService = biomarkerTrendService;
    }

    @GetMapping("/summary")
    public ResponseEntity<PatientDashboardSummaryDto> getDashboardSummary(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required to access dashboard summary");
        }
        PatientDashboardSummaryDto summary = biomarkerTrendService.getDashboardSummary(principal.getId());
        return ResponseEntity.ok(summary);
    }
}
