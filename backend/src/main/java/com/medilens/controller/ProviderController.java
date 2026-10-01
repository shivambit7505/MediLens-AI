package com.medilens.controller;

import com.medilens.dto.provider.CareNavigationResponseDto;
import com.medilens.dto.provider.ProviderDto;
import com.medilens.security.UserPrincipal;
import com.medilens.service.DoctorFinderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/providers")
public class ProviderController {

    private final DoctorFinderService doctorFinderService;

    public ProviderController(DoctorFinderService doctorFinderService) {
        this.doctorFinderService = doctorFinderService;
    }

    @GetMapping("/recommendations")
    public ResponseEntity<CareNavigationResponseDto> getCareNavigation(@AuthenticationPrincipal UserPrincipal principal) {
        UUID userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(doctorFinderService.getCareNavigationForUser(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProviderDto>> searchProviders(
            @RequestParam(value = "specialty", required = false) String specialty,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "telehealthOnly", required = false, defaultValue = "false") Boolean telehealthOnly
    ) {
        return ResponseEntity.ok(doctorFinderService.searchProviders(specialty, query, telehealthOnly));
    }
}
