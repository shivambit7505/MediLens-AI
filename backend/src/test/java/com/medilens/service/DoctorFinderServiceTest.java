package com.medilens.service;

import com.medilens.dto.provider.CareNavigationResponseDto;
import com.medilens.dto.provider.ProviderDto;
import com.medilens.repository.MeasurementRepository;
import com.medilens.repository.ReportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorFinderServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private MeasurementRepository measurementRepository;

    @InjectMocks
    private DoctorFinderService doctorFinderService;

    @Test
    @DisplayName("Should return accredited provider directory filtered by specialty")
    void testSearchProvidersBySpecialty() {
        List<ProviderDto> results = doctorFinderService.searchProviders("Cardiology", null, false);
        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(p -> p.getSpecialty().equalsIgnoreCase("Cardiology"));
    }

    @Test
    @DisplayName("Should filter providers by telehealth availability")
    void testSearchProvidersTelehealthOnly() {
        List<ProviderDto> results = doctorFinderService.searchProviders(null, null, true);
        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(ProviderDto::isTelehealthAvailable);
    }

    @Test
    @DisplayName("Should default to Internal Medicine recommendation when no reports available")
    void testCareNavigationDefaultRoutine() {
        java.util.UUID userId = java.util.UUID.randomUUID();
        when(reportRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId)).thenReturn(Collections.emptyList());

        CareNavigationResponseDto response = doctorFinderService.getCareNavigationForUser(userId);
        assertThat(response.getRecommendations()).isNotEmpty();
        assertThat(response.getRecommendations().get(0).getSpecialty()).isEqualTo("Internal Medicine");
    }
}
