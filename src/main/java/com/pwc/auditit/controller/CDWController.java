package com.pwc.auditit.controller;
import jakarta.validation.Valid;
import com.pwc.auditit.dto.request.CDWCreateRequest;
import com.pwc.auditit.dto.request.CDWUpdateRequest;
import com.pwc.auditit.dto.response.CDWListItem;
import com.pwc.auditit.dto.response.CDWResponse;
import com.pwc.auditit.service.CDWService;
import com.pwc.auditit.dto.response.ApiResponse;
import org.springframework.security.core.Authentication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/cdws")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class CDWController {
    
    private static final String ANONYMOUS_USER = "ANONYMOUS";
    private final CDWService cdwService;
    
    public CDWController(CDWService cdwService) {
        this.cdwService = cdwService;
    }
    
    /**
     * Helper method to get current user from authentication
     * Extracts email from authenticated Profile
     */
    private String getCurrentUser(UserDetails userDetails) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            
            // If it's a Profile object (from JwtAuthenticationFilter)
            if (principal instanceof com.pwc.auditit.entity.Profile) {
                com.pwc.auditit.entity.Profile profile = (com.pwc.auditit.entity.Profile) principal;
                String email = profile.getEmail();
                if (email != null && !email.isEmpty()) {
                    return email;
                }
            }
        }
        
        // If still no user, throw exception (should not happen with proper auth)
        throw new IllegalStateException("User must be authenticated to perform this action");
    }
    
    // ==================== GET ENDPOINTS ====================
    
    /**
     * Search CDWs for a specific mission
     */
    @GetMapping("/search/{missionId}")
    public ResponseEntity<ApiResponse<List<CDWListItem>>> searchCDWs(
            @PathVariable String missionId,
            @RequestParam String query) {
        List<CDWListItem> results = cdwService.searchCDWs(missionId, query);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }
    
    /**
     * Get all CDWs for a specific mission with pagination and optional filters
     */
    @GetMapping("/mission/{missionId}")
    public ResponseEntity<ApiResponse<Page<CDWListItem>>> getCDWsByMission(
            @PathVariable String missionId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            Pageable pageable) {
        Page<CDWListItem> cdws = cdwService.getCDWsByMissionWithFilters(missionId, status, severity, pageable);
        return ResponseEntity.ok(ApiResponse.ok(cdws));
    }
    
    /**
     * Get a single CDW by ID and Mission ID
     */
    @GetMapping("/{missionId}/{cdwId}")
    public ResponseEntity<ApiResponse<CDWResponse>> getCDWById(
            @PathVariable String missionId,
            @PathVariable String cdwId) {
        CDWResponse cdw = cdwService.getCDWById(cdwId, missionId);
        return ResponseEntity.ok(ApiResponse.ok(cdw));
    }
    
     /**
     * Get CDW count for a mission
     */
    @GetMapping("/count/{missionId}")
    public ResponseEntity<ApiResponse<Long>> getCDWCount(
            @PathVariable String missionId) {
        long count = cdwService.getCDWCountByMission(missionId);
        return ResponseEntity.ok(ApiResponse.ok(count));
    }
    
    /**
     * Get a batch of CDWs by a list of IDs for a specific mission (GET with query params)
     */
    @GetMapping("/{missionId}/batch")
    public ResponseEntity<ApiResponse<List<CDWResponse>>> getCDWsBatchGet(
            @PathVariable String missionId,
            @RequestParam List<String> ids) {
        List<CDWResponse> cdws = cdwService.getCDWsByIds(ids, missionId);
        return ResponseEntity.ok(ApiResponse.ok(cdws));
    }
    
    /**
     * Get a batch of CDWs by a list of IDs for a specific mission (POST with body)
     */
    @PostMapping("/{missionId}/batch")
    public ResponseEntity<ApiResponse<List<CDWResponse>>> getCDWsBatchPost(
            @PathVariable String missionId,
            @RequestBody List<String> ids) {
        List<CDWResponse> cdws = cdwService.getCDWsByIds(ids, missionId);
        return ResponseEntity.ok(ApiResponse.ok(cdws));
    }
    
    // ==================== POST ENDPOINTS ====================
    
    /**
     * Create a single CDW
     */
    @PostMapping("/{missionId}")
    public ResponseEntity<ApiResponse<CDWResponse>> createCDW(
            @PathVariable String missionId,
            @Valid @RequestBody CDWCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String currentUser = getCurrentUser(userDetails);
        request.setMissionId(missionId);
        CDWResponse cdw = cdwService.createCDW(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(cdw));
    }
    
    /**
     * Create multiple CDWs in a mission (bulk add)
     */
    @PostMapping("/{missionId}/bulk-add")
    public ResponseEntity<ApiResponse<List<CDWResponse>>> createCDWsBulk(
            @PathVariable String missionId,
            @Valid @RequestBody List<CDWCreateRequest> requests,
            @AuthenticationPrincipal UserDetails userDetails) {
        String currentUser = getCurrentUser(userDetails);
        List<CDWResponse> cdws = cdwService.createCDWsBulk(requests, missionId, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(cdws));
    }
    
    /**
     * Delete multiple CDWs in bulk
     */
    @PostMapping("/{missionId}/bulk-delete")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteCDWsBulk(
            @PathVariable String missionId,
            @RequestBody List<String> ids) {
        cdwService.deleteCDWs(ids, missionId);
        Map<String, Object> response = Map.of(
            "entity", "CDW",
            "action", "delete",
            "count", ids.size(),
            "missionId", missionId,
            "status", "success"
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
    
    // ==================== PUT ENDPOINTS ====================
    
    /**
     * Update an existing CDW
     */
    @PutMapping("/{missionId}/{cdwId}")
    public ResponseEntity<ApiResponse<CDWResponse>> updateCDW(
            @PathVariable String missionId,
            @PathVariable String cdwId,
            @Valid @RequestBody CDWUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String currentUser = getCurrentUser(userDetails);
        CDWResponse cdw = cdwService.updateCDW(cdwId, missionId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(cdw));
    }
    
    // ==================== DELETE ENDPOINTS ====================
    
    /**
     * Delete a single CDW by ID
     */
    @DeleteMapping("/{missionId}/{cdwId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteCDW(
            @PathVariable String missionId,
            @PathVariable String cdwId) {
        cdwService.deleteCDW(cdwId, missionId);
        Map<String, Object> response = Map.of(
            "entity", "CDW",
            "action", "delete",
            "id", cdwId,
            "missionId", missionId,
            "status", "success"
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Delete all CDWs for a mission
     */
    @DeleteMapping("/mission/{missionId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteAllCDWsByMission(
            @PathVariable String missionId) {
        long deletedCount = cdwService.deleteAllByMission(missionId);
        Map<String, Object> response = Map.of(
                "entity", "CDW",
                "action", "deleteAll",
                "missionId", missionId,
                "deletedCount", deletedCount,
                "status", "success"
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

}



