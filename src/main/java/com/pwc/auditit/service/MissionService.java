package com.pwc.auditit.service;

import com.pwc.auditit.dto.request.CreateMissionRequest;
import com.pwc.auditit.dto.request.UpdateMissionStatusRequest;
import com.pwc.auditit.dto.response.MissionResponse;
import com.pwc.auditit.dto.response.MissionSummaryResponse;
import com.pwc.auditit.entity.enums.MissionStatus;

import java.util.List;
import java.util.UUID;

public interface MissionService {
    MissionResponse createMission(CreateMissionRequest request, UUID currentUserId);
    MissionResponse getMissionById(UUID id);
    List<MissionResponse> getAllMissions(String societe, String exercice, MissionStatus statut);
    List<MissionResponse> getAccessibleMissions(UUID userId);
    MissionResponse updateMissionStatus(UUID id, UpdateMissionStatusRequest request);
    MissionSummaryResponse getMissionSummary(UUID missionId);
    void deleteMission(UUID id);
    void addTeamMember(UUID missionId, UUID userId);
    void removeTeamMember(UUID missionId, UUID userId);
    void recalculateProgress(UUID missionId);
}
