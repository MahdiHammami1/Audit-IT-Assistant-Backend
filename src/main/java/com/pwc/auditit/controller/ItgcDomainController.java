package com.pwc.auditit.controller;

import com.pwc.auditit.dto.request.BulkCreateControlRequest;
import com.pwc.auditit.dto.request.CreateItgcDomainRequest;
import com.pwc.auditit.dto.response.ApiResponse;
import com.pwc.auditit.dto.response.ControlResponse;
import com.pwc.auditit.dto.response.ItgcDomainResponse;
import com.pwc.auditit.service.ItgcDomainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/itgc-domains")
@RequiredArgsConstructor
@Tag(name = "ITGC Reference Data", description = "ITGC domains and controls reference")
public class ItgcDomainController {

    private final ItgcDomainService domainService;

    @GetMapping
    @Operation(summary = "Get all ITGC domains with their controls")
    public ResponseEntity<ApiResponse<List<ItgcDomainResponse>>> getAllDomains() {
        return ResponseEntity.ok(ApiResponse.ok(domainService.getAllDomains()));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Create multiple ITGC domains in bulk")
    public ResponseEntity<ApiResponse<List<ItgcDomainResponse>>> createDomains(
            @RequestBody List<CreateItgcDomainRequest> request) {
        return ResponseEntity.ok(ApiResponse.ok(domainService.createDomains(request)));
    }

    @GetMapping("/{code}/controls")
    @Operation(summary = "Get controls for a specific ITGC domain")
    public ResponseEntity<ApiResponse<List<ControlResponse>>> getControlsByDomain(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok(domainService.getControlsByDomain(code)));
    }

    @PostMapping("/controls/bulk")
    @Operation(summary = "Create multiple controls for a specific ITGC domain")
    public ResponseEntity<ApiResponse<List<ControlResponse>>> createControlsForDomain(
            @RequestBody BulkCreateControlRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(domainService.createControlsForDomain(request)));
    }

    @DeleteMapping("/controls/{controlId}")
    @Operation(summary = "Delete a specific control by ID")
    public ResponseEntity<ApiResponse<Void>> deleteControl(@PathVariable UUID controlId) {
        domainService.deleteControl(controlId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/all")
    @Operation(summary = "Delete all ITGC domains")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteAllDomains() {
        long deletedCount = domainService.deleteAll();
        Map<String, Object> response = Map.of(
                "entity", "ItgcDomain",
                "deletedCount", deletedCount,
                "status", "success"
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
