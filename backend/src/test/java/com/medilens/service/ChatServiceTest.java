package com.medilens.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilens.client.AiChatResponse;
import com.medilens.client.AiRagResponse;
import com.medilens.client.AiServiceClient;
import com.medilens.dto.chat.ConversationDetailDto;
import com.medilens.dto.chat.CreateConversationRequest;
import com.medilens.dto.chat.MessageDto;
import com.medilens.dto.chat.SendMessageRequest;
import com.medilens.model.Conversation;
import com.medilens.model.Message;
import com.medilens.model.Role;
import com.medilens.model.User;
import com.medilens.repository.ConversationRepository;
import com.medilens.repository.MeasurementRepository;
import com.medilens.repository.MessageRepository;
import com.medilens.repository.ReportRepository;
import com.medilens.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private MeasurementRepository measurementRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AiServiceClient aiServiceClient;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ChatService chatService;

    private User user;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(
                conversationRepository,
                messageRepository,
                reportRepository,
                measurementRepository,
                userRepository,
                aiServiceClient,
                objectMapper
        );
        user = new User(UUID.randomUUID(), "test@medilens.ai", "hash", "Jane", "Doe",
                LocalDate.of(1990, 5, 12), "FEMALE", Role.ROLE_PATIENT, true, Instant.now(), Instant.now());
        conversation = new Conversation(UUID.randomUUID(), user, null, "General Consultation", Instant.now(), Instant.now());
    }

    @Test
    @DisplayName("Should create conversation successfully")
    void testCreateConversation() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);
        when(conversationRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(any())).thenReturn(Collections.emptyList());

        CreateConversationRequest req = new CreateConversationRequest(null, "Test Chat", null);
        ConversationDetailDto result = chatService.createConversation(user.getId(), req);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("General Consultation");
    }

    @Test
    @DisplayName("Should send message, call AI service, and record assistant response with citations")
    void testSendMessageAndReceiveGroundedReply() {
        when(conversationRepository.findByIdAndUserId(conversation.getId(), user.getId()))
                .thenReturn(Optional.of(conversation));
        when(reportRepository.findTop5ByUserIdOrderByCreatedAtDesc(user.getId()))
                .thenReturn(Collections.emptyList());
        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId()))
                .thenReturn(Collections.emptyList());

        AiChatResponse mockAiRes = new AiChatResponse(
                "Elevated fasting glucose is evaluated sequentially according to ADA standards.",
                List.of(new AiRagResponse.AiEvidenceSource("ADA-2024", "ADA Standards", "ADA Guidelines", "Endocrinology")),
                List.of("What lifestyle changes can I make?"),
                "Educational disclaimer",
                true
        );
        when(aiServiceClient.chat(any())).thenReturn(mockAiRes);

        Message savedAssistant = new Message(
                UUID.randomUUID(), conversation, "ASSISTANT",
                mockAiRes.getReply(),
                "[\"[ADA-2024] ADA Standards (ADA Guidelines)\"]",
                "[]",
                Instant.now()
        );
        when(messageRepository.save(any(Message.class))).thenReturn(savedAssistant);

        SendMessageRequest req = new SendMessageRequest("What does high glucose mean?");
        MessageDto reply = chatService.sendMessage(user.getId(), conversation.getId(), req);

        assertThat(reply.getSender()).isEqualTo("ASSISTANT");
        assertThat(reply.getContent()).contains("ADA standards");
        assertThat(reply.getCitedSources()).isNotEmpty();
    }
}
