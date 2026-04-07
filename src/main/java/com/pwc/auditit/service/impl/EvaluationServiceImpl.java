package com.pwc.auditit.service.impl;

import com.pwc.auditit.dto.request.ValidateEvaluationRequest;
import com.pwc.auditit.dto.response.EvaluationResponse;
import com.pwc.auditit.dto.response.ProfileResponse;
import com.pwc.auditit.entity.Evaluation;
import com.pwc.auditit.entity.Profile;
import com.pwc.auditit.exception.ResourceNotFoundException;
import com.pwc.auditit.repository.EvaluationRepository;
import com.pwc.auditit.repository.ProfileRepository;
import com.pwc.auditit.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final ProfileRepository profileRepository;

    @Override
    @Transactional(readOnly = true)
    public EvaluationResponse getEvaluationByTestResult(UUID testResultId) {
        return evaluationRepository.findByTestResultId(testResultId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation for TestResult", testResultId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluationResponse> getEvaluationsByMission(UUID missionId) {
        return evaluationRepository.findByMissionId(missionId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public EvaluationResponse validateEvaluation(UUID evaluationId, ValidateEvaluationRequest request, UUID validatorId) {
        Evaluation eval = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation", evaluationId));
        Profile validator = profileRepository.findById(validatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", validatorId));

        eval.setValidationStatus(request.getValidationStatus());
        eval.setValidatedBy(validator);
        eval.setValidatedAt(Instant.now());
        eval.setValidationComment(request.getValidationComment());

        return toResponse(evaluationRepository.save(eval));
    }

    private EvaluationResponse toResponse(Evaluation e) {
        Profile vb = e.getValidatedBy();
        return EvaluationResponse.builder()
                .id(e.getId())
                .testResultId(e.getTestResult().getId())
                .result(e.getResult())
                .aiSummary(e.getAiSummary())
                .deficiencyType(e.getDeficiencyType())
                .impact(e.getImpact())
                .recommendations(e.getRecommendations())
                .validationStatus(e.getValidationStatus())
                .validatedBy(vb == null ? null : ProfileResponse.builder()
                        .id(vb.getId()).email(vb.getEmail())
                        .fullName(vb.getFullName()).initials(vb.getInitials()).build())
                .validatedAt(e.getValidatedAt())
                .validationComment(e.getValidationComment())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
