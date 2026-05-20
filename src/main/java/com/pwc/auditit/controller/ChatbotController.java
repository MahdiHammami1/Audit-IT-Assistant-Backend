package com.pwc.auditit.controller;

import com.pwc.auditit.client.FastApiClient;
import com.pwc.auditit.dto.ChatbotRequest;
import com.pwc.auditit.dto.ChatbotResponse;
import com.pwc.auditit.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final FastApiClient fastApiClient;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatbotResponse>> askChatbot(@Valid @RequestBody ChatbotRequest request) {
        try {
            ChatbotResponse response = fastApiClient.askChatbot(request);
            return ResponseEntity.ok(ApiResponse.ok("Chatbot response generated", response));
        } catch (Exception e) {
            log.error("Chatbot request failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to ask chatbot: " + e.getMessage()));
        }
    }
}
