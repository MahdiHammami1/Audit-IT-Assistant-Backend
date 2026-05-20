package com.pwc.auditit.controller;

import com.pwc.auditit.dto.request.ValidateEvaluationRequest;
import com.pwc.auditit.dto.response.ApiResponse;
import com.pwc.auditit.dto.response.EvaluationResponse;
import com.pwc.auditit.entity.Profile;
import com.pwc.auditit.security.CurrentUser;
import com.pwc.auditit.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/evaluations")
@RequiredArgsConstructor
@Tag(name = "Evaluations", description = "AI evaluation results and validation")
public class EvaluationController {

    private final EvaluationService evaluationService;

    @GetMapping
    @Operation(summary = "Get all evaluations")
    public ResponseEntity<ApiResponse<List<EvaluationResponse>>> getAllEvaluations() {
        return ResponseEntity.ok(ApiResponse.ok(evaluationService.getAllEvaluations()));
    }

    @PostMapping("/{testResultId}")
    @Operation(summary = "Create or get AI evaluation for a test result")
    public ResponseEntity<ApiResponse<EvaluationResponse>> createEvaluation(@PathVariable UUID testResultId) {
        return ResponseEntity.ok(ApiResponse.ok(evaluationService.getEvaluationByTestResult(testResultId)));
    }

    @GetMapping("/mission/{missionId}")
    @Operation(summary = "Get all evaluations for a mission")
    public ResponseEntity<ApiResponse<List<EvaluationResponse>>> getByMission(@PathVariable UUID missionId) {
        return ResponseEntity.ok(ApiResponse.ok(evaluationService.getEvaluationsByMission(missionId)));
    }

    @GetMapping("/test-result/{testResultId}")
    @Operation(summary = "Get evaluation for a specific test result")
    public ResponseEntity<ApiResponse<EvaluationResponse>> getByTestResult(@PathVariable UUID testResultId) {
        return ResponseEntity.ok(ApiResponse.ok(evaluationService.getEvaluationByTestResult(testResultId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get evaluation by ID")
    public ResponseEntity<ApiResponse<EvaluationResponse>> getEvaluation(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(evaluationService.getEvaluationById(id)));
    }

    @PatchMapping("/{evaluationId}/validate")
    @Operation(summary = "Validate or reject an AI evaluation")
    public ResponseEntity<ApiResponse<EvaluationResponse>> validate(
            @PathVariable UUID evaluationId,
            @Valid @RequestBody ValidateEvaluationRequest request,
            @CurrentUser Profile currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(
                evaluationService.validateEvaluation(evaluationId, request, currentUser.getId())));
    }

    @DeleteMapping("/all")
    @Operation(summary = "Delete all evaluations")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteAllEvaluations() {
        long deletedCount = evaluationService.deleteAll();
        Map<String, Object> response = Map.of(
                "entity", "Evaluation",
                "deletedCount", deletedCount,
                "status", "success"
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
