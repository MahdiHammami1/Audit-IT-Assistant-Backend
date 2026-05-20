package com.pwc.auditit.service.impl;

import com.pwc.auditit.dto.request.CreateProfileRequest;
import com.pwc.auditit.dto.request.UpdateProfileRequest;
import com.pwc.auditit.dto.response.ProfileResponse;
import com.pwc.auditit.entity.Profile;
import com.pwc.auditit.entity.UserRole;
import com.pwc.auditit.entity.enums.AppRole;
import com.pwc.auditit.exception.ResourceNotFoundException;
import com.pwc.auditit.repository.ProfileRepository;
import com.pwc.auditit.repository.UserRoleRepository;
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
    private final UserRoleRepository userRoleRepository;

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
    public List<ProfileResponse> getAuditeurs() {
        // Get all user roles with auditor role
        List<UserRole> auditorRoles = userRoleRepository.findByRole(AppRole.auditor);

        // Extract unique user IDs and fetch their profiles
        return auditorRoles.stream()
                .map(UserRole::getUser)
                .distinct()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProfileResponse createProfile(CreateProfileRequest request) {
        // Create new profile
        Profile profile = Profile.builder()
                .id(UUID.randomUUID())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .fullName(request.getFirstName() + " " + request.getLastName())
                .avatarUrl(request.getAvatarUrl())
                .isVerified(false)
                .build();

        Profile savedProfile = profileRepository.save(profile);
        return toResponse(savedProfile);
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
            profile.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            profile.setLastName(request.getLastName().trim());
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
        if (request.getRoles() != null) {
            updateUserRoles(profile, request.getRoles());
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
            profile.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            profile.setLastName(request.getLastName().trim());
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
        if (request.getRoles() != null) {
            updateUserRoles(profile, request.getRoles());
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

    @Override
    @Transactional
    public long deleteAll() {
        long count = profileRepository.count();
        profileRepository.deleteAll();
        return count;
    }

    @Override
    @Transactional
    public ProfileResponse updateUserRoles(UUID userId, java.util.Set<String> roleNames) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", userId));

        // Delete all existing roles for this user
        List<UserRole> existingRoles = userRoleRepository.findByUserId(profile.getId());
        userRoleRepository.deleteAll(existingRoles);

        // Create new roles and add to profile
        java.util.Set<UserRole> newRoles = new java.util.HashSet<>();
        for (String roleName : roleNames) {
            try {
                AppRole appRole = AppRole.valueOf(roleName.toLowerCase());
                UserRole userRole = UserRole.builder()
                        .id(UUID.randomUUID())
                        .user(profile)
                        .role(appRole)
                        .build();
                UserRole savedRole = userRoleRepository.save(userRole);
                newRoles.add(savedRole);
            } catch (IllegalArgumentException e) {
                // Skip invalid role names
                continue;
            }
        }

        // Update the profile with new roles
        profile.setRoles(newRoles);
        profileRepository.save(profile);

        // Reload the profile to ensure fresh data
        profile = profileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", userId));

        return toResponse(profile);
    }

    private ProfileResponse toResponse(Profile p) {
        return ProfileResponse.builder()
                .id(p.getId())
                .email(p.getEmail())
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .fullName(p.getFullName())
                .initials(p.getInitials())
                .avatarUrl(p.getAvatarUrl())
                .roles(p.getRoles().stream().map(r -> r.getRole()).collect(Collectors.toSet()))
                .build();
    }

    private void updateUserRoles(Profile profile, java.util.Set<String> roleNames) {
        // Delete all existing roles for this user
        List<UserRole> existingRoles = userRoleRepository.findByUserId(profile.getId());
        userRoleRepository.deleteAll(existingRoles);

        // Create new roles and add to profile
        java.util.Set<UserRole> newRoles = new java.util.HashSet<>();
        for (String roleName : roleNames) {
            try {
                AppRole appRole = AppRole.valueOf(roleName.toLowerCase());
                UserRole userRole = UserRole.builder()
                        .id(UUID.randomUUID())
                        .user(profile)
                        .role(appRole)
                        .build();
                UserRole savedRole = userRoleRepository.save(userRole);
                newRoles.add(savedRole);
            } catch (IllegalArgumentException e) {
                // Skip invalid role names
                continue;
            }
        }

        // Update the profile with new roles
        profile.setRoles(newRoles);
    }
}
