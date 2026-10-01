package com.medilens.controller;

import com.medilens.dto.chat.*;
import com.medilens.exception.UnauthorizedException;
import com.medilens.security.UserPrincipal;
import com.medilens.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDto>> getUserConversations(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required to access clinical chat");
        }
        return ResponseEntity.ok(chatService.getUserConversations(principal.getId()));
    }

    @PostMapping("/conversations")
    public ResponseEntity<ConversationDetailDto> createConversation(
            @RequestBody CreateConversationRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required to start clinical conversation");
        }
        ConversationDetailDto created = chatService.createConversation(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<ConversationDetailDto> getConversationDetail(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required to view conversation");
        }
        return ResponseEntity.ok(chatService.getConversationDetail(principal.getId(), id));
    }

    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<MessageDto> sendMessage(
            @PathVariable("id") UUID id,
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required to send message");
        }
        return ResponseEntity.ok(chatService.sendMessage(principal.getId(), id, request));
    }

    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required to delete conversation");
        }
        chatService.deleteConversation(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
