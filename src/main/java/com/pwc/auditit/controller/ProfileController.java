package com.pwc.auditit.controller;

import com.pwc.auditit.dto.request.CreateProfileRequest;
import com.pwc.auditit.dto.request.UpdateProfileRequest;
import com.pwc.auditit.dto.response.ApiResponse;
import com.pwc.auditit.dto.response.ProfileResponse;
import com.pwc.auditit.entity.Profile;
import com.pwc.auditit.security.CurrentUser;
import com.pwc.auditit.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
@Tag(name = "Profiles", description = "User profile management")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> getCurrentProfile(@CurrentUser Profile currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.getCurrentUserProfile(currentUser.getId())));
    }

    @GetMapping
    @Operation(summary = "Get all user profiles")
    public ResponseEntity<ApiResponse<List<ProfileResponse>>> getAllProfiles() {
        return ResponseEntity.ok(ApiResponse.ok(profileService.getAllProfiles()));
    }

    @PostMapping
    @Operation(summary = "Create a new user profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> createProfile(
            @Valid @RequestBody CreateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.createProfile(request)));
    }

    @GetMapping("/auditors")
    @Operation(summary = "Get all auditors")
    public ResponseEntity<ApiResponse<List<ProfileResponse>>> getAuditeurs() {
        return ResponseEntity.ok(ApiResponse.ok(profileService.getAuditeurs()));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateCurrentProfile(
            @CurrentUser Profile currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.updateProfile(currentUser.getId(), request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get profile by ID")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.getProfile(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update profile by ID (full update)")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.updateProfile(id, request)));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partial update profile by ID")
    public ResponseEntity<ApiResponse<ProfileResponse>> partialUpdateProfile(
            @PathVariable UUID id,
            @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.partialUpdateProfile(id, request)));
    }

    @PutMapping("/me/roles")
    @Operation(summary = "Update current user roles")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateCurrentUserRoles(
            @CurrentUser Profile currentUser,
            @RequestBody java.util.Set<String> roleNames) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.updateUserRoles(currentUser.getId(), roleNames)));
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Update user roles by ID")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateUserRoles(
            @PathVariable UUID id,
            @RequestBody java.util.Set<String> roleNames) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.updateUserRoles(id, roleNames)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete profile by ID")
    public ResponseEntity<ApiResponse<Void>> deleteProfile(@PathVariable UUID id) {
        profileService.deleteProfile(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/batch")
    @Operation(summary = "Delete multiple profiles by IDs")
    public ResponseEntity<ApiResponse<Void>> deleteProfilesBatch(@RequestBody List<UUID> ids) {
        profileService.deleteProfilesBatch(ids);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/all")
    @Operation(summary = "Delete all profiles")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteAllProfiles() {
        long deletedCount = profileService.deleteAll();
        Map<String, Object> response = Map.of(
                "entity", "Profile",
                "deletedCount", deletedCount,
                "status", "success"
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
