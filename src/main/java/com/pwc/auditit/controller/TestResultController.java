package com.pwc.auditit.controller;

import com.pwc.auditit.dto.request.SaveTestFieldValuesRequest;
import com.pwc.auditit.dto.response.ApiResponse;
import com.pwc.auditit.dto.response.TestResultResponse;
import com.pwc.auditit.entity.Profile;
import com.pwc.auditit.security.CurrentUser;
import com.pwc.auditit.service.TestResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/test-results")
@RequiredArgsConstructor
@Tag(name = "Test Results", description = "ITGC control test management")
public class TestResultController {

    private final TestResultService testResultService;

    @GetMapping("/mission/{missionId}")
    @Operation(summary = "Get all test results for a mission")
    public ResponseEntity<ApiResponse<List<TestResultResponse>>> getByMission(@PathVariable UUID missionId) {
        return ResponseEntity.ok(ApiResponse.ok(testResultService.getTestResultsByMission(missionId)));
    }

    @GetMapping("/mission/{missionId}/application/{applicationId}")
    @Operation(summary = "Get test results for a specific application in a mission")
    public ResponseEntity<ApiResponse<List<TestResultResponse>>> getByMissionAndApp(
            @PathVariable UUID missionId, @PathVariable UUID applicationId) {
        return ResponseEntity.ok(ApiResponse.ok(
                testResultService.getTestResultsByMissionAndApplication(missionId, applicationId)));
    }

    @GetMapping("/{testResultId}")
    @Operation(summary = "Get a single test result")
    public ResponseEntity<ApiResponse<TestResultResponse>> getTestResult(@PathVariable UUID testResultId) {
        return ResponseEntity.ok(ApiResponse.ok(testResultService.getTestResult(testResultId)));
    }

    @PostMapping("/mission/{missionId}/application/{applicationId}/control/{controlId}")
    @Operation(summary = "Get or create test result for a control")
    public ResponseEntity<ApiResponse<TestResultResponse>> getOrCreate(
            @PathVariable UUID missionId,
            @PathVariable UUID applicationId,
            @PathVariable UUID controlId) {
        return ResponseEntity.ok(ApiResponse.ok(
                testResultService.getOrCreateTestResult(missionId, applicationId, controlId)));
    }

    @PutMapping("/{testResultId}/field-values")
    @Operation(summary = "Save field values for a test")
    public ResponseEntity<ApiResponse<TestResultResponse>> saveFieldValues(
            @PathVariable UUID testResultId,
            @RequestBody SaveTestFieldValuesRequest request,
            @CurrentUser Profile currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(
                testResultService.saveFieldValues(testResultId, request, currentUser.getId())));
    }

    @PostMapping("/{testResultId}/complete")
    @Operation(summary = "Mark a test as complete")
    public ResponseEntity<ApiResponse<TestResultResponse>> markComplete(@PathVariable UUID testResultId) {
        return ResponseEntity.ok(ApiResponse.ok(testResultService.markComplete(testResultId)));
    }
}
