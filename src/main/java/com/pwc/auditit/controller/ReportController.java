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

    // ...existing code...

    @PatchMapping("/{reportId}/sections/{sectionId}")
    @Operation(summary = "Edit a report section")
    public ResponseEntity<ApiResponse<ReportResponse>> updateSection(
            @PathVariable UUID reportId,
            @PathVariable UUID sectionId,
            @RequestBody UpdateReportSectionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.updateSection(reportId, sectionId, request)));
    }

    @PostMapping("/{reportId}/finalize")
    @Operation(summary = "Finalize a report")
    public ResponseEntity<ApiResponse<Void>> finalizeReport(@PathVariable UUID reportId) {
        reportService.finalizeReport(reportId);
        return ResponseEntity.ok(ApiResponse.ok("Report finalized", null));
    }


}
