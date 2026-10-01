package com.medilens.controller;

import com.medilens.dto.triage.TriageEvaluationRequest;
import com.medilens.dto.triage.TriageEvaluationResponse;
import com.medilens.dto.triage.TriageRuleDto;
import com.medilens.security.UserPrincipal;
import com.medilens.service.TriageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/triage")
public class TriageController {

    private final TriageService triageService;

    public TriageController(TriageService triageService) {
        this.triageService = triageService;
    }

    @GetMapping("/rules")
    public ResponseEntity<List<TriageRuleDto>> getActiveRules() {
        return ResponseEntity.ok(triageService.getAllActiveRules());
    }

    @PostMapping("/evaluate")
    public ResponseEntity<TriageEvaluationResponse> evaluate(
            @RequestBody TriageEvaluationRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UUID userId = principal != null ? principal.getId() : null;
        TriageEvaluationResponse response = triageService.evaluateTriage(userId, request);
        return ResponseEntity.ok(response);
    }
}
