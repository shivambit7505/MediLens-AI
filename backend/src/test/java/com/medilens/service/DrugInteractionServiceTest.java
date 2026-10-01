package com.medilens.service;

import com.medilens.dto.medication.InteractionCheckResponse;
import com.medilens.model.DrugInteraction;
import com.medilens.model.InteractionSeverity;
import com.medilens.model.Medication;
import com.medilens.repository.DrugInteractionRepository;
import com.medilens.repository.MedicationRepository;
import com.medilens.repository.UserMedicationRepository;
import com.medilens.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DrugInteractionServiceTest {

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private UserMedicationRepository userMedicationRepository;

    @Mock
    private DrugInteractionRepository drugInteractionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DrugInteractionService drugInteractionService;

    private Medication warfarin;
    private Medication ibuprofen;
    private Medication aspirin;

    @BeforeEach
    void setUp() {
        warfarin = new Medication(UUID.randomUUID(), "Coumadin", "Warfarin", "11289", "Anticoagulant", "Titrated INR", Instant.now());
        ibuprofen = new Medication(UUID.randomUUID(), "Advil", "Ibuprofen", "5640", "NSAID", "200-400mg PRN", Instant.now());
        aspirin = new Medication(UUID.randomUUID(), "Bayer", "Aspirin", "1191", "Antiplatelet", "81mg daily", Instant.now());
    }

    @Test
    @DisplayName("Should detect CONTRAINDICATED interaction between Warfarin and Ibuprofen")
    void testContraindicatedDrugInteraction() {
        DrugInteraction interaction = new DrugInteraction(
                UUID.randomUUID(), warfarin, ibuprofen,
                InteractionSeverity.CONTRAINDICATED,
                "Severe bleeding risk from displacement and mucosal erosions.",
                "FDA Safety Labeling", Instant.now()
        );

        when(drugInteractionRepository.findInteractionBetween(warfarin.getId(), ibuprofen.getId()))
                .thenReturn(Optional.of(interaction));

        InteractionCheckResponse response = drugInteractionService.checkInteractionsForMedicationIds(
                List.of(warfarin.getId(), ibuprofen.getId())
        );

        assertThat(response.isHasInteractions()).isTrue();
        assertThat(response.getHighestSeverity()).isEqualTo(InteractionSeverity.CONTRAINDICATED);
        assertThat(response.getHighestSeverityBadgeColor()).isEqualTo("red");
        assertThat(response.getInteractions()).hasSize(1);
    }

    @Test
    @DisplayName("Should return no interactions when fewer than 2 medications provided")
    void testSingleMedicationNoInteractions() {
        InteractionCheckResponse response = drugInteractionService.checkInteractionsForMedicationIds(
                List.of(warfarin.getId())
        );

        assertThat(response.isHasInteractions()).isFalse();
        assertThat(response.getTotalInteractionsCount()).isEqualTo(0);
    }
}
