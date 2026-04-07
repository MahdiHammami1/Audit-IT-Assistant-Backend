package com.pwc.auditit.controller;

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

    // ...existing code...

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
}
