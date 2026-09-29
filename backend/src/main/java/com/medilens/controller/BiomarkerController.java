package com.medilens.controller;

import com.medilens.dto.trend.BiomarkerHistoryResponseDto;
import com.medilens.dto.trend.BiomarkerSummaryDto;
import com.medilens.exception.UnauthorizedException;
import com.medilens.security.UserPrincipal;
import com.medilens.service.BiomarkerTrendService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/biomarkers")
public class BiomarkerController {

    private final BiomarkerTrendService biomarkerTrendService;

    public BiomarkerController(BiomarkerTrendService biomarkerTrendService) {
        this.biomarkerTrendService = biomarkerTrendService;
    }

    @GetMapping
    public ResponseEntity<List<BiomarkerSummaryDto>> getAllBiomarkers() {
        return ResponseEntity.ok(biomarkerTrendService.getAllBiomarkers());
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<BiomarkerHistoryResponseDto> getBiomarkerHistory(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required to access biomarker history");
        }
        BiomarkerHistoryResponseDto history = biomarkerTrendService.getBiomarkerHistory(id, principal.getId());
        return ResponseEntity.ok(history);
    }
}
