package com.pwc.auditit.controller;

import com.pwc.auditit.dto.request.CreateMissionRequest;
import com.pwc.auditit.dto.request.UpdateMissionStatusRequest;
import com.pwc.auditit.dto.response.ApiResponse;
import com.pwc.auditit.dto.response.MissionResponse;
import com.pwc.auditit.dto.response.MissionSummaryResponse;
import com.pwc.auditit.entity.Profile;
import com.pwc.auditit.entity.enums.MissionStatus;
import com.pwc.auditit.security.CurrentUser;
import com.pwc.auditit.service.MissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/missions")
@RequiredArgsConstructor
@Tag(name = "Missions", description = "ITGC Audit mission management")
public class MissionController {

    private final MissionService missionService;

    @PostMapping
    @Operation(summary = "Create a new audit mission")
    public ResponseEntity<ApiResponse<MissionResponse>> createMission(
            @Valid @RequestBody CreateMissionRequest request,
            @CurrentUser Profile currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Mission created", missionService.createMission(request, currentUser.getId())));
    }

    @GetMapping
    @Operation(summary = "Get all missions with optional filters")
    public ResponseEntity<ApiResponse<List<MissionResponse>>> getAllMissions(
            @RequestParam(required = false) String societe,
            @RequestParam(required = false) String exercice,
            @RequestParam(required = false) MissionStatus statut) {
        return ResponseEntity.ok(ApiResponse.ok(missionService.getAllMissions(societe, exercice, statut)));
    }

    @GetMapping("/my-missions")
    @Operation(summary = "Get missions accessible by current user")
    public ResponseEntity<ApiResponse<List<MissionResponse>>> getMyMissions(
            @CurrentUser Profile currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(missionService.getAccessibleMissions(currentUser.getId())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get mission by ID")
    public ResponseEntity<ApiResponse<MissionResponse>> getMission(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(missionService.getMissionById(id)));
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "Get mission completion summary and KPIs")
    public ResponseEntity<ApiResponse<MissionSummaryResponse>> getMissionSummary(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(missionService.getMissionSummary(id)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update mission status")
    public ResponseEntity<ApiResponse<MissionResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMissionStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(missionService.updateMissionStatus(id, request)));
    }

    @PostMapping("/{id}/team/{userId}")
    @Operation(summary = "Add team member to mission")
    public ResponseEntity<ApiResponse<Void>> addTeamMember(
            @PathVariable UUID id, @PathVariable UUID userId) {
        missionService.addTeamMember(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Team member added", null));
    }

    @DeleteMapping("/{id}/team/{userId}")
    @Operation(summary = "Remove team member from mission")
    public ResponseEntity<ApiResponse<Void>> removeTeamMember(
            @PathVariable UUID id, @PathVariable UUID userId) {
        missionService.removeTeamMember(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Team member removed", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete mission")
    public ResponseEntity<ApiResponse<Void>> deleteMission(@PathVariable UUID id) {
        missionService.deleteMission(id);
        return ResponseEntity.ok(ApiResponse.ok("Mission deleted", null));
    }
}
