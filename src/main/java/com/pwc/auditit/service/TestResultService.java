package com.pwc.auditit.service;

import com.pwc.auditit.dto.request.SaveTestFieldValuesRequest;
import com.pwc.auditit.dto.response.TestResultResponse;

import java.util.List;
import java.util.UUID;

public interface TestResultService {
    TestResultResponse getOrCreateTestResult(UUID missionId, UUID applicationId, UUID controlId);
    TestResultResponse getTestResult(UUID testResultId);
    List<TestResultResponse> getTestResultsByMission(UUID missionId);
    List<TestResultResponse> getTestResultsByMissionAndApplication(UUID missionId, UUID applicationId);
    TestResultResponse saveFieldValues(UUID testResultId, SaveTestFieldValuesRequest request, UUID currentUserId);
    TestResultResponse markComplete(UUID testResultId);
}
