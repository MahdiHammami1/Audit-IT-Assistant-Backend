package com.pwc.auditit.controller;

import com.pwc.auditit.dto.request.UpdateReportSectionRequest;
import com.pwc.auditit.dto.response.ApiResponse;
import com.pwc.auditit.dto.response.ReportResponse;
import com.pwc.auditit.entity.Profile;
import com.pwc.auditit.security.CurrentUser;
import com.pwc.auditit.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "ITGC/ITAC report generation and management")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/mission/{missionId}/generate")
    @Operation(summary = "Generate ITGC report for a mission")
    public ResponseEntity<ApiResponse<ReportResponse>> generateReport(
            @PathVariable UUID missionId,
            @CurrentUser Profile currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.generateReport(missionId, currentUser.getId())));
    }

    @GetMapping
    @Operation(summary = "Get all reports")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getAllReports() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getAllReports()));
    }

    @GetMapping("/mission/{missionId}")
    @Operation(summary = "Get all reports for a mission")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getByMission(@PathVariable UUID missionId) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getReportsByMission(missionId)));
    }

    @GetMapping("/missions/{missionId}")
    @Operation(summary = "Get all reports for a mission (alternative endpoint)")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getReportsByMission(@PathVariable UUID missionId) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getReportsByMission(missionId)));
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "Get a report by ID")
    public ResponseEntity<ApiResponse<ReportResponse>> getReport(@PathVariable String reportId) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getReport(UUID.fromString(reportId))));
    }

    @PatchMapping("/{reportId}/sections/{sectionId}")
    @Operation(summary = "Edit a report section")
    public ResponseEntity<ApiResponse<ReportResponse>> updateSection(
            @PathVariable String reportId,
            @PathVariable String sectionId,
            @RequestBody UpdateReportSectionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.updateSection(UUID.fromString(reportId), UUID.fromString(sectionId), request)));
    }

    @PostMapping("/{reportId}/finalize")
    @Operation(summary = "Finalize a report")
    public ResponseEntity<ApiResponse<Void>> finalizeReport(@PathVariable String reportId) {
        reportService.finalizeReport(UUID.fromString(reportId));
        return ResponseEntity.ok(ApiResponse.ok("Report finalized", null));
    }

    @DeleteMapping("/all")
    @Operation(summary = "Delete all reports")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteAllReports() {
        long deletedCount = reportService.deleteAll();
        Map<String, Object> response = Map.of(
                "entity", "Report",
                "deletedCount", deletedCount,
                "status", "success"
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
