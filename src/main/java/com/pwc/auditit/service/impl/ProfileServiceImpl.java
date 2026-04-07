package com.pwc.auditit.service.impl;

import com.pwc.auditit.dto.request.UpdateProfileRequest;
import com.pwc.auditit.dto.response.ProfileResponse;
import com.pwc.auditit.entity.Profile;
import com.pwc.auditit.exception.ResourceNotFoundException;
import com.pwc.auditit.repository.ProfileRepository;
import com.pwc.auditit.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;

    @Override
    public ProfileResponse getProfile(UUID userId) {
        return toResponse(profileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", userId)));
    }

    @Override
    public ProfileResponse getCurrentUserProfile(UUID userId) {
        return getProfile(userId);
    }

    @Override
    public List<ProfileResponse> getAllProfiles() {
        return profileRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(UUID id, UpdateProfileRequest request) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", id));

        // Update all fields
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            profile.setEmail(request.getEmail());
        }
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            profile.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            profile.setLastName(request.getLastName());
        }
        if ((request.getFirstName() != null && !request.getFirstName().isBlank()) ||
            (request.getLastName() != null && !request.getLastName().isBlank())) {
            String firstName = request.getFirstName() != null ? request.getFirstName() : profile.getFirstName();
            String lastName = request.getLastName() != null ? request.getLastName() : profile.getLastName();
            profile.setFullName(firstName + " " + lastName);
        }
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }

        profileRepository.save(profile);
        return toResponse(profile);
    }

    @Override
    @Transactional
    public ProfileResponse partialUpdateProfile(UUID id, UpdateProfileRequest request) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", id));

        // Partial update - only update fields that are not null
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            profile.setEmail(request.getEmail());
        }
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            profile.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            profile.setLastName(request.getLastName());
        }
        // Update fullName if either firstName or lastName is provided
        if ((request.getFirstName() != null && !request.getFirstName().isBlank()) ||
            (request.getLastName() != null && !request.getLastName().isBlank())) {
            String firstName = request.getFirstName() != null ? request.getFirstName() : profile.getFirstName();
            String lastName = request.getLastName() != null ? request.getLastName() : profile.getLastName();
            profile.setFullName(firstName + " " + lastName);
        }
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }

        profileRepository.save(profile);
        return toResponse(profile);
    }

    @Override
    @Transactional
    public void deleteProfile(UUID id) {
        if (!profileRepository.existsById(id)) {
            throw new ResourceNotFoundException("Profile", id);
        }
        profileRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteProfilesBatch(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        profileRepository.deleteAllById(ids);
    }

    private ProfileResponse toResponse(Profile p) {
        return ProfileResponse.builder()
                .id(p.getId()).email(p.getEmail())
                .fullName(p.getFullName()).initials(p.getInitials())
                .avatarUrl(p.getAvatarUrl())
                .roles(p.getRoles().stream().map(r -> r.getRole()).collect(Collectors.toSet()))
                .build();
    }
}
