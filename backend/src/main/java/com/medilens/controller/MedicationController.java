package com.medilens.controller;

import com.medilens.dto.medication.*;
import com.medilens.exception.UnauthorizedException;
import com.medilens.security.UserPrincipal;
import com.medilens.service.DrugInteractionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/medications")
public class MedicationController {

    private final DrugInteractionService drugInteractionService;

    public MedicationController(DrugInteractionService drugInteractionService) {
        this.drugInteractionService = drugInteractionService;
    }

    @GetMapping
    public ResponseEntity<List<MedicationDto>> searchCatalog(@RequestParam(value = "query", required = false) String query) {
        return ResponseEntity.ok(drugInteractionService.searchMedications(query));
    }

    @GetMapping("/user")
    public ResponseEntity<List<UserMedicationDto>> getUserMedications(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required to access user medications");
        }
        return ResponseEntity.ok(drugInteractionService.getUserActiveMedications(principal.getId()));
    }

    @PostMapping("/user")
    public ResponseEntity<UserMedicationDto> addUserMedication(
            @Valid @RequestBody AddUserMedicationRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required to add user medication");
        }
        UserMedicationDto created = drugInteractionService.addUserMedication(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<Void> removeUserMedication(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required to remove medication");
        }
        drugInteractionService.removeUserMedication(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/check-interactions")
    public ResponseEntity<InteractionCheckResponse> checkInteractions(@RequestBody InteractionCheckRequest request) {
        return ResponseEntity.ok(drugInteractionService.checkInteractionsForMedicationIds(request.getMedicationIds()));
    }

    @GetMapping("/user/check-interactions")
    public ResponseEntity<InteractionCheckResponse> checkUserActiveInteractions(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required to evaluate user interactions");
        }
        return ResponseEntity.ok(drugInteractionService.checkInteractionsForUser(principal.getId()));
    }
}
