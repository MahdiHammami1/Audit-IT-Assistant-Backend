package com.pwc.auditit.service.impl;

import com.pwc.auditit.dto.request.SaveTestFieldValuesRequest;
import com.pwc.auditit.dto.response.*;
import com.pwc.auditit.entity.*;
import com.pwc.auditit.entity.enums.TestStatus;
import com.pwc.auditit.exception.ResourceNotFoundException;
import com.pwc.auditit.repository.*;
import com.pwc.auditit.service.MissionService;
import com.pwc.auditit.service.TestResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TestResultServiceImpl implements TestResultService {

    private final TestResultRepository testResultRepository;
    private final MissionRepository missionRepository;
    private final ApplicationRepository applicationRepository;
    private final ControlRepository controlRepository;
    private final ControlFieldRepository controlFieldRepository;
    private final TestFieldValueRepository fieldValueRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final EvaluationRepository evaluationRepository;
    private final MissionService missionService;

    @Override
    public TestResultResponse getOrCreateTestResult(UUID missionId, UUID applicationId, UUID controlId) {
        return testResultRepository
                .findByMissionIdAndApplicationIdAndControlId(missionId, applicationId, controlId)
                .map(this::toResponse)
                .orElseGet(() -> {
                    Mission mission = missionRepository.findById(missionId)
                            .orElseThrow(() -> new ResourceNotFoundException("Mission", missionId));
                    Application app = applicationRepository.findById(applicationId)
                            .orElseThrow(() -> new ResourceNotFoundException("Application", applicationId));
                    Control control = controlRepository.findById(controlId)
                            .orElseThrow(() -> new ResourceNotFoundException("Control", controlId));
                    TestResult tr = TestResult.builder()
                            .mission(mission).application(app).control(control).build();
                    return toResponse(testResultRepository.save(tr));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public TestResultResponse getTestResult(UUID testResultId) {
        return toResponse(testResultRepository.findById(testResultId)
                .orElseThrow(() -> new ResourceNotFoundException("TestResult", testResultId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestResultResponse> getTestResultsByMission(UUID missionId) {
        return testResultRepository.findByMissionId(missionId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestResultResponse> getTestResultsByMissionAndApplication(UUID missionId, UUID applicationId) {
        return testResultRepository.findByMissionAndApplication(missionId, applicationId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public TestResultResponse saveFieldValues(UUID testResultId, SaveTestFieldValuesRequest request, UUID currentUserId) {
        TestResult tr = testResultRepository.findById(testResultId)
                .orElseThrow(() -> new ResourceNotFoundException("TestResult", testResultId));

        if (request.getValues() != null) {
            for (SaveTestFieldValuesRequest.FieldValueEntry entry : request.getValues()) {
                ControlField field = controlFieldRepository.findById(entry.getControlFieldId())
                        .orElseThrow(() -> new ResourceNotFoundException("ControlField", entry.getControlFieldId()));

                TestFieldValue fv = fieldValueRepository
                        .findByTestResultIdAndControlFieldId(testResultId, entry.getControlFieldId())
                        .orElse(TestFieldValue.builder().testResult(tr).controlField(field).build());

                fv.setValueText(entry.getValueText());
                fv.setValueNumber(entry.getValueNumber());
                fv.setValueDate(entry.getValueDate());
                fieldValueRepository.save(fv);
            }
        }

        if (tr.getStatut() == TestStatus.NON_TESTE) {
            tr.setStatut(TestStatus.EN_COURS);
            testResultRepository.save(tr);
        }

        return toResponse(testResultRepository.findById(testResultId).orElseThrow());
    }

    @Override
    public TestResultResponse markComplete(UUID testResultId) {
        TestResult tr = testResultRepository.findById(testResultId)
                .orElseThrow(() -> new ResourceNotFoundException("TestResult", testResultId));
        tr.setStatut(TestStatus.COMPLETE);
        TestResult saved = testResultRepository.save(tr);
        missionService.recalculateProgress(tr.getMission().getId());
        return toResponse(saved);
    }

    private TestResultResponse toResponse(TestResult tr) {
        Control c = tr.getControl();
        return TestResultResponse.builder()
                .id(tr.getId())
                .missionId(tr.getMission().getId())
                .applicationId(tr.getApplication().getId())
                .applicationName(tr.getApplication().getName())
                .control(ControlResponse.builder()
                        .id(c.getId()).code(c.getCode())
                        .domainCode(c.getDomain().getCode())
                        .title(c.getTitle()).description(c.getDescription())
                        .orderIndex(c.getOrderIndex())
                        .fields(controlFieldRepository.findByControlIdOrderByOrderIndexAsc(c.getId()).stream()
                                .map(f -> ControlFieldResponse.builder()
                                        .id(f.getId()).label(f.getLabel())
                                        .fieldType(f.getFieldType()).isRequired(f.getIsRequired())
                                        .orderIndex(f.getOrderIndex()).build())
                                .collect(Collectors.toList()))
                        .build())
                .statut(tr.getStatut())
                .createdAt(tr.getCreatedAt())
                .updatedAt(tr.getUpdatedAt())
                .fieldValues(fieldValueRepository.findByTestResultId(tr.getId()).stream()
                        .map(fv -> TestFieldValueResponse.builder()
                                .id(fv.getId())
                                .controlFieldId(fv.getControlField().getId())
                                .fieldLabel(fv.getControlField().getLabel())
                                .valueText(fv.getValueText())
                                .valueNumber(fv.getValueNumber())
                                .valueDate(fv.getValueDate()).build())
                        .collect(Collectors.toList()))
                .uploadedFiles(uploadedFileRepository.findByTestResultId(tr.getId()).stream()
                        .map(f -> UploadedFileResponse.builder()
                                .id(f.getId())
                                .controlFieldId(f.getControlField().getId())
                                .fileName(f.getFileName()).filePath(f.getFilePath())
                                .fileSize(f.getFileSize()).mimeType(f.getMimeType())
                                .uploadedAt(f.getUploadedAt()).build())
                        .collect(Collectors.toList()))
                .evaluation(evaluationRepository.findByTestResultId(tr.getId())
                        .map(e -> EvaluationResponse.builder()
                                .id(e.getId()).testResultId(tr.getId())
                                .result(e.getResult()).aiSummary(e.getAiSummary())
                                .deficiencyType(e.getDeficiencyType()).impact(e.getImpact())
                                .recommendations(e.getRecommendations())
                                .validationStatus(e.getValidationStatus())
                                .validatedAt(e.getValidatedAt())
                                .validationComment(e.getValidationComment())
                                .createdAt(e.getCreatedAt()).build())
                        .orElse(null))
                .build();
    }
}
