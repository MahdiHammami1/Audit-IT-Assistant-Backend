package com.pwc.auditit.service;

import com.pwc.auditit.dto.request.CreateProfileRequest;
import com.pwc.auditit.dto.request.UpdateProfileRequest;
import com.pwc.auditit.dto.response.ProfileResponse;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ProfileService {
    ProfileResponse getProfile(UUID userId);
    ProfileResponse getCurrentUserProfile(UUID userId);
    List<ProfileResponse> getAllProfiles();
    List<ProfileResponse> getAuditeurs();
    ProfileResponse createProfile(CreateProfileRequest request);
    ProfileResponse updateProfile(UUID id, UpdateProfileRequest request);
    ProfileResponse partialUpdateProfile(UUID id, UpdateProfileRequest request);
    ProfileResponse updateUserRoles(UUID userId, Set<String> roleNames);
    void deleteProfile(UUID id);
    void deleteProfilesBatch(List<UUID> ids);
    long deleteAll();
}
