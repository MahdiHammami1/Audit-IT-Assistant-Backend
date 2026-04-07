package com.pwc.auditit.service.impl;

import com.pwc.auditit.dto.request.CreateMissionRequest;
import com.pwc.auditit.dto.request.UpdateMissionStatusRequest;
import com.pwc.auditit.dto.response.ApplicationResponse;
import com.pwc.auditit.dto.response.MissionResponse;
import com.pwc.auditit.dto.response.MissionSummaryResponse;
import com.pwc.auditit.dto.response.ProfileResponse;
import com.pwc.auditit.entity.*;
import com.pwc.auditit.entity.enums.EvaluationResult;
import com.pwc.auditit.entity.enums.MissionStatus;
import com.pwc.auditit.entity.enums.TestStatus;
import com.pwc.auditit.entity.enums.ValidationStatus;
import com.pwc.auditit.exception.ResourceNotFoundException;
import com.pwc.auditit.repository.*;
import com.pwc.auditit.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionServiceImpl implements MissionService {

    private final MissionRepository missionRepository;
    private final ProfileRepository profileRepository;
    private final MissionTeamMemberRepository teamMemberRepository;
    private final ApplicationRepository applicationRepository;
    private final TestResultRepository testResultRepository;
    private final EvaluationRepository evaluationRepository;

    @Override
    public MissionResponse createMission(CreateMissionRequest request, UUID currentUserId) {
        Profile responsible = profileRepository.findById(request.getAuditeurResponsableId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile", request.getAuditeurResponsableId()));

        Mission mission = Mission.builder()
                .societe(request.getSociete())
                .exercice(request.getExercice())
                .typeRapport(request.getTypeRapport())
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .auditeurResponsable(responsible)
                .notes(request.getNotes())
                .build();

        Mission saved = missionRepository.save(mission);

        // Add team members
        if (request.getTeamMemberIds() != null) {
            request.getTeamMemberIds().forEach(memberId -> {
                Profile member = profileRepository.findById(memberId)
                        .orElseThrow(() -> new ResourceNotFoundException("Profile", memberId));
                MissionTeamMember teamMember = MissionTeamMember.builder()
                        .mission(saved).user(member).build();
                teamMemberRepository.save(teamMember);
            });
        }

        // Create applications
        if (request.getApplications() != null) {
            request.getApplications().forEach(appReq -> {
                Application app = Application.builder()
                        .mission(saved)
                        .name(appReq.getName())
                        .type(appReq.getType())
                        .domains(appReq.getDomains() != null ? appReq.getDomains() : List.of())
                        .build();
                applicationRepository.save(app);
            });
        }

        return toResponse(missionRepository.findById(saved.getId()).orElseThrow());
    }

    @Override
    @Transactional(readOnly = true)
    public MissionResponse getMissionById(UUID id) {
        return toResponse(missionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mission", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MissionResponse> getAllMissions(String societe, String exercice, MissionStatus statut) {
        return missionRepository.findWithFilters(societe, exercice, statut)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MissionResponse> getAccessibleMissions(UUID userId) {
        return missionRepository.findAccessibleByUser(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public MissionResponse updateMissionStatus(UUID id, UpdateMissionStatusRequest request) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mission", id));
        mission.setStatut(request.getStatut());
        return toResponse(missionRepository.save(mission));
    }

    @Override
    @Transactional(readOnly = true)
    public MissionSummaryResponse getMissionSummary(UUID missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new ResourceNotFoundException("Mission", missionId));

        long total = testResultRepository.countByMissionId(missionId);
        long completed = testResultRepository.countByMissionIdAndStatut(missionId, TestStatus.COMPLETE);
        long defMin = evaluationRepository.findByMissionIdAndResult(missionId, EvaluationResult.DEFICIENCE_MINEURE).size();
        long defMaj = evaluationRepository.findByMissionIdAndResult(missionId, EvaluationResult.DEFICIENCE_MAJEURE).size();
        long pending = evaluationRepository.countByTestResultMissionIdAndValidationStatus(missionId, ValidationStatus.EN_ATTENTE);

        return MissionSummaryResponse.builder()
                .missionId(missionId)
                .societe(mission.getSociete())
                .exercice(mission.getExercice())
                .typeRapport(mission.getTypeRapport())
                .statut(mission.getStatut())
                .progress(mission.getProgress())
                .totalTests(total)
                .completedTests(completed)
                .deficiencesMineures(defMin)
                .deficiencesMajeures(defMaj)
                .pendingValidations(pending)
                .build();
    }

    @Override
    public void deleteMission(UUID id) {
        if (!missionRepository.existsById(id)) throw new ResourceNotFoundException("Mission", id);
        missionRepository.deleteById(id);
    }

    @Override
    public void addTeamMember(UUID missionId, UUID userId) {
        if (teamMemberRepository.existsByMissionIdAndUserId(missionId, userId)) return;
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new ResourceNotFoundException("Mission", missionId));
        Profile user = profileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", userId));
        teamMemberRepository.save(MissionTeamMember.builder().mission(mission).user(user).build());
    }

    @Override
    public void removeTeamMember(UUID missionId, UUID userId) {
        teamMemberRepository.deleteByMissionIdAndUserId(missionId, userId);
    }

    @Override
    public void recalculateProgress(UUID missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new ResourceNotFoundException("Mission", missionId));
        long total = testResultRepository.countByMissionId(missionId);
        long completed = testResultRepository.countByMissionIdAndStatut(missionId, TestStatus.COMPLETE);
        int progress = total == 0 ? 0 : (int) Math.round(100.0 * completed / total);
        mission.setProgress(progress);
        if (progress == 0) mission.setStatut(MissionStatus.EN_ATTENTE);
        else if (progress == 100) mission.setStatut(MissionStatus.RAPPORT_GENERE);
        else mission.setStatut(MissionStatus.EN_COURS);
        missionRepository.save(mission);
    }

    private MissionResponse toResponse(Mission mission) {
        return MissionResponse.builder()
                .id(mission.getId())
                .societe(mission.getSociete())
                .exercice(mission.getExercice())
                .typeRapport(mission.getTypeRapport())
                .dateDebut(mission.getDateDebut())
                .dateFin(mission.getDateFin())
                .auditeurResponsable(toProfileResponse(mission.getAuditeurResponsable()))
                .notes(mission.getNotes())
                .statut(mission.getStatut())
                .progress(mission.getProgress())
                .createdAt(mission.getCreatedAt())
                .updatedAt(mission.getUpdatedAt())
                .teamMembers(teamMemberRepository.findByMissionId(mission.getId()).stream()
                        .map(m -> toProfileResponse(m.getUser())).collect(Collectors.toList()))
                .applications(applicationRepository.findByMissionIdOrderByCreatedAtAsc(mission.getId()).stream()
                        .map(this::toApplicationResponse).collect(Collectors.toList()))
                .build();
    }

    private ProfileResponse toProfileResponse(Profile p) {
        if (p == null) return null;
        return ProfileResponse.builder()
                .id(p.getId()).email(p.getEmail())
                .fullName(p.getFullName()).initials(p.getInitials())
                .avatarUrl(p.getAvatarUrl())
                .roles(p.getRoles().stream().map(r -> r.getRole()).collect(Collectors.toSet()))
                .build();
    }

    private ApplicationResponse toApplicationResponse(Application a) {
        return ApplicationResponse.builder()
                .id(a.getId()).missionId(a.getMission().getId())
                .name(a.getName()).type(a.getType())
                .domains(a.getDomains()).statut(a.getStatut())
                .createdAt(a.getCreatedAt()).build();
    }
}
