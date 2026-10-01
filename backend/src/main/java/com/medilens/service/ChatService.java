package com.medilens.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilens.client.AiChatRequest;
import com.medilens.client.AiChatResponse;
import com.medilens.client.AiMeasurementDto;
import com.medilens.client.AiServiceClient;
import com.medilens.dto.chat.*;
import com.medilens.exception.ResourceNotFoundException;
import com.medilens.model.Conversation;
import com.medilens.model.Measurement;
import com.medilens.model.Message;
import com.medilens.model.Report;
import com.medilens.model.User;
import com.medilens.repository.ConversationRepository;
import com.medilens.repository.MeasurementRepository;
import com.medilens.repository.MessageRepository;
import com.medilens.repository.ReportRepository;
import com.medilens.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ReportRepository reportRepository;
    private final MeasurementRepository measurementRepository;
    private final UserRepository userRepository;
    private final AiServiceClient aiServiceClient;
    private final ObjectMapper objectMapper;

    private static final List<String> DEFAULT_SUGGESTED_PROMPTS = List.of(
            "What do my latest glucose and HbA1c results mean for my health?",
            "Why is my serum creatinine or potassium flagged?",
            "What specific questions should I ask my doctor during my next visit?",
            "Are there dietary or lifestyle adjustments supported by clinical evidence for my results?"
    );

    public ChatService(ConversationRepository conversationRepository,
                       MessageRepository messageRepository,
                       ReportRepository reportRepository,
                       MeasurementRepository measurementRepository,
                       UserRepository userRepository,
                       AiServiceClient aiServiceClient,
                       ObjectMapper objectMapper) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.reportRepository = reportRepository;
        this.measurementRepository = measurementRepository;
        this.userRepository = userRepository;
        this.aiServiceClient = aiServiceClient;
        this.objectMapper = objectMapper;
    }

    public List<ConversationDto> getUserConversations(UUID userId) {
        List<Conversation> conversations = conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        List<ConversationDto> result = new ArrayList<>();

        for (Conversation c : conversations) {
            List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(c.getId());
            String lastSnippet = "";
            if (!messages.isEmpty()) {
                String full = messages.get(messages.size() - 1).getContent();
                lastSnippet = full.length() > 60 ? full.substring(0, 57) + "..." : full;
            }
            result.add(new ConversationDto(
                    c.getId(),
                    c.getReport() != null ? c.getReport().getId() : null,
                    c.getTitle(),
                    messages.size(),
                    lastSnippet,
                    c.getUpdatedAt()
            ));
        }

        return result;
    }

    public ConversationDetailDto getConversationDetail(UUID userId, UUID conversationId) {
        Conversation c = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(c.getId());
        List<MessageDto> messageDtos = messages.stream().map(this::mapMessageToDto).toList();

        return new ConversationDetailDto(
                c.getId(),
                c.getReport() != null ? c.getReport().getId() : null,
                c.getReport() != null ? c.getReport().getOriginalFilename() : null,
                c.getTitle(),
                messageDtos,
                DEFAULT_SUGGESTED_PROMPTS,
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }

    @Transactional
    public ConversationDetailDto createConversation(UUID userId, CreateConversationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Report report = null;
        if (request.getReportId() != null) {
            report = reportRepository.findByIdAndUserId(request.getReportId(), userId)
                    .orElse(null);
        }

        String title = request.getTitle();
        if (title == null || title.isBlank()) {
            title = report != null ? "Discussion: " + report.getOriginalFilename() : "Clinical Health Consultation";
        }

        Conversation conversation = new Conversation();
        conversation.setUser(user);
        conversation.setReport(report);
        conversation.setTitle(title);
        conversation = conversationRepository.save(conversation);

        // If initial message provided, process it immediately
        if (request.getInitialMessage() != null && !request.getInitialMessage().isBlank()) {
            sendMessage(userId, conversation.getId(), new SendMessageRequest(request.getInitialMessage()));
        }

        return getConversationDetail(userId, conversation.getId());
    }

    @Transactional
    public MessageDto sendMessage(UUID userId, UUID conversationId, SendMessageRequest request) {
        Conversation c = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

        // 1. Save USER message
        Message userMsg = new Message();
        userMsg.setConversation(c);
        userMsg.setSender("USER");
        userMsg.setContent(request.getContent());
        messageRepository.save(userMsg);

        // 2. Collect Biomarkers Context
        List<AiChatRequest.AiChatBiomarkerDto> recentBiomarkers = new ArrayList<>();
        UUID targetReportId = c.getReport() != null ? c.getReport().getId() : null;

        if (targetReportId == null) {
            List<Report> userReports = reportRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
            if (!userReports.isEmpty()) {
                targetReportId = userReports.get(0).getId();
            }
        }

        if (targetReportId != null) {
            List<Measurement> measurements = measurementRepository.findByReportIdAndUserId(targetReportId, userId);
            for (Measurement m : measurements) {
                String cName = m.getBiomarker() != null ? m.getBiomarker().getCanonicalName() : m.getExtractedName();
                Double val = m.getNormalizedValueNumeric() != null ? m.getNormalizedValueNumeric().doubleValue() :
                        (m.getObservedValueNumeric() != null ? m.getObservedValueNumeric().doubleValue() : 0.0);
                String unit = m.getNormalizedUnit() != null ? m.getNormalizedUnit() : m.getExtractedUnit();

                recentBiomarkers.add(new AiChatRequest.AiChatBiomarkerDto(
                        cName,
                        val,
                        unit != null ? unit : "",
                        m.getStatus().name()
                ));
            }
        }

        // 3. Build Chat History for Context
        List<Message> historyMessages = messageRepository.findByConversationIdOrderByCreatedAtAsc(c.getId());
        List<AiChatRequest.ChatMessagePayload> historyPayload = new ArrayList<>();

        int startIndex = Math.max(0, historyMessages.size() - 6);
        for (int i = startIndex; i < historyMessages.size(); i++) {
            Message h = historyMessages.get(i);
            historyPayload.add(new AiChatRequest.ChatMessagePayload(
                    h.getSender().toLowerCase(),
                    h.getContent()
            ));
        }

        // 4. Call AI Service RAG Chat
        AiChatRequest chatReq = new AiChatRequest(
                request.getContent(),
                "Patient User ID: " + userId,
                recentBiomarkers,
                historyPayload
        );

        String assistantReply;
        List<String> sourcesList = new ArrayList<>();

        try {
            AiChatResponse aiResponse = aiServiceClient.chat(chatReq);
            assistantReply = aiResponse.getReply();
            if (aiResponse.getCitedSources() != null) {
                for (var s : aiResponse.getCitedSources()) {
                    sourcesList.add(String.format("[%s] %s (%s)", s.chunkId(), s.title(), s.source()));
                }
            }
        } catch (Exception ex) {
            log.warn("AI Service RAG chat call failed, falling back to deterministic clinical guidance: {}", ex.getMessage());
            assistantReply = "According to clinical laboratory standards, observed values should be evaluated sequentially in correlation with your clinical history. Please consult your physician regarding persistent abnormal results.\n\n[ADA-2024-GLUCOSE] American Diabetes Association Standards of Care 2024.";
            sourcesList.add("[ADA-2024-GLUCOSE] American Diabetes Association Standards of Care 2024");
        }

        // 5. Save ASSISTANT message
        Message assistantMsg = new Message();
        assistantMsg.setConversation(c);
        assistantMsg.setSender("ASSISTANT");
        assistantMsg.setContent(assistantReply);

        try {
            assistantMsg.setSourcesJson(objectMapper.writeValueAsString(sourcesList));
        } catch (Exception e) {
            assistantMsg.setSourcesJson("[]");
        }

        Message savedAssistant = messageRepository.save(assistantMsg);

        // Update conversation timestamp
        c.setUpdatedAt(Instant.now());
        conversationRepository.save(c);

        return mapMessageToDto(savedAssistant);
    }

    @Transactional
    public void deleteConversation(UUID userId, UUID conversationId) {
        Conversation c = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));
        conversationRepository.delete(c);
    }

    private MessageDto mapMessageToDto(Message m) {
        List<String> sources = new ArrayList<>();
        if (m.getSourcesJson() != null && !m.getSourcesJson().isBlank()) {
            try {
                sources = objectMapper.readValue(m.getSourcesJson(), new TypeReference<List<String>>() {});
            } catch (Exception ignored) {}
        }

        return new MessageDto(
                m.getId(),
                m.getSender(),
                m.getContent(),
                sources,
                m.getCreatedAt()
        );
    }
}
