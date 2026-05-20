package com.pwc.auditit.service;

import com.pwc.auditit.dto.request.ValidateEvaluationRequest;
import com.pwc.auditit.dto.response.EvaluationResponse;

import java.util.List;
import java.util.UUID;

public interface EvaluationService {
    EvaluationResponse getEvaluationByTestResult(UUID testResultId);
    EvaluationResponse getEvaluationById(UUID id);
    List<EvaluationResponse> getAllEvaluations();
    List<EvaluationResponse> getEvaluationsByMission(UUID missionId);
    EvaluationResponse validateEvaluation(UUID evaluationId, ValidateEvaluationRequest request, UUID validatorId);
    long deleteAll();
}
