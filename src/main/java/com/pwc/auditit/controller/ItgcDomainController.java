package com.pwc.auditit.controller;

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

    @GetMapping("/{code}/controls")
    @Operation(summary = "Get controls for a specific ITGC domain")
    public ResponseEntity<ApiResponse<List<ControlResponse>>> getControlsByDomain(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok(domainService.getControlsByDomain(code)));
    }
}
