package com.pwc.auditit.service;

import com.pwc.auditit.dto.request.UpdateProfileRequest;
import com.pwc.auditit.dto.response.ProfileResponse;
import java.util.List;
import java.util.UUID;

public interface ProfileService {
    ProfileResponse getProfile(UUID userId);
    ProfileResponse getCurrentUserProfile(UUID userId);
    List<ProfileResponse> getAllProfiles();
    ProfileResponse updateProfile(UUID id, UpdateProfileRequest request);
    ProfileResponse partialUpdateProfile(UUID id, UpdateProfileRequest request);
    void deleteProfile(UUID id);
    void deleteProfilesBatch(List<UUID> ids);
}
