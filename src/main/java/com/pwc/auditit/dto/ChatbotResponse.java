package com.pwc.auditit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ChatbotResponse {

    private boolean success;
    private String agent;

    @JsonProperty("session_id")
    private String sessionId;

    private String answer;
    private List<SourceReference> sources;

    @JsonProperty("detected_intent")
    private String detectedIntent;

    private String confidence;

    @JsonProperty("requires_more_context")
    private boolean requiresMoreContext;

    @JsonProperty("follow_up_questions")
    private List<String> followUpQuestions;

    private ChatbotTrace trace;

    @Data
    public static class SourceReference {
        @JsonProperty("chunk_id")
        private String chunkId;
        private String title;
        @JsonProperty("source_file")
        private String sourceFile;
        @JsonProperty("source_type")
        private String sourceType;
        private String feature;
        @JsonProperty("report_id")
        private String reportId;
        @JsonProperty("mission_id")
        private String missionId;
        @JsonProperty("blob_path")
        private String blobPath;
        private Double score;
        private String excerpt;
    }

    @Data
    public static class ChatbotTrace {
        @JsonProperty("retrieved_chunks")
        private int retrievedChunks;
        @JsonProperty("used_rag")
        private boolean usedRag;
        @JsonProperty("index_name")
        private String indexName;
        @JsonProperty("embedding_model")
        private String embeddingModel;
        @JsonProperty("chat_model")
        private String chatModel;
    }
}
