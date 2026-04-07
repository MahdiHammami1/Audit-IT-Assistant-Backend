package com.pwc.auditit.service.impl;

import com.pwc.auditit.dto.request.UpdateReportSectionRequest;
import com.pwc.auditit.dto.response.ReportResponse;
import com.pwc.auditit.dto.response.ReportSectionResponse;
import com.pwc.auditit.entity.*;
import com.pwc.auditit.entity.enums.*;
import com.pwc.auditit.exception.BusinessException;
import com.pwc.auditit.exception.ResourceNotFoundException;
import com.pwc.auditit.repository.*;
import com.pwc.auditit.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ReportSectionRepository sectionRepository;
    private final MissionRepository missionRepository;
    private final ProfileRepository profileRepository;
    private final EvaluationRepository evaluationRepository;

    @Override
    public ReportResponse generateReport(UUID missionId, UUID currentUserId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new ResourceNotFoundException("Mission", missionId));
        Profile generator = profileRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", currentUserId));

        Report report = Report.builder()
                .mission(mission)
                .type(mission.getTypeRapport())
                .title("Rapport ITGC — " + mission.getSociete() + " — " + mission.getExercice())
                .generatedBy(generator)
                .build();
        Report saved = reportRepository.save(report);

        // Generate sections from evaluations
        List<Evaluation> evals = evaluationRepository.findByMissionId(missionId);
        generateSections(saved, mission, evals);

        return toResponse(reportRepository.findById(saved.getId()).orElseThrow());
    }

    private void generateSections(Report report, Mission mission, List<Evaluation> evals) {
        long defMin = evals.stream().filter(e -> e.getResult() == EvaluationResult.DEFICIENCE_MINEURE).count();
        long defMaj = evals.stream().filter(e -> e.getResult() == EvaluationResult.DEFICIENCE_MAJEURE).count();

        String opinion = defMaj > 0 || defMin >= 3 ? "Insuffisant" : defMin > 0 ? "Partiellement efficace" : "Efficace";

        sectionRepository.save(ReportSection.builder().report(report).orderIndex(1)
                .title("Préambule")
                .content("Dans le cadre de notre mission d'audit des états financiers de " + mission.getSociete()
                        + " pour l'exercice " + mission.getExercice()
                        + ", nous avons procédé à l'évaluation des contrôles généraux informatiques (ITGC).")
                .build());

        sectionRepository.save(ReportSection.builder().report(report).orderIndex(2)
                .title("Opinion globale sur les ITGC")
                .content("Sur la base de nos travaux, notre opinion sur l'efficacité des ITGC est : " + opinion
                        + ". " + defMaj + " déficience(s) majeure(s) et " + defMin + " déficience(s) mineure(s) ont été identifiées.")
                .build());

        sectionRepository.save(ReportSection.builder().report(report).orderIndex(3)
                .title("Approche d'audit")
                .content("Nos travaux ont porté sur les domaines ITGC suivants : Accès aux programmes et données (APD), "
                        + "Program Change (PC) et Computer Operations (CO), conformément à la méthodologie PwC.")
                .build());

        int[] idx = {4};
        evals.stream()
                .filter(e -> e.getResult() != EvaluationResult.CONFORME)
                .forEach(e -> {
                    String controlCode = e.getTestResult().getControl().getCode();
                    sectionRepository.save(ReportSection.builder().report(report)
                            .orderIndex(idx[0]++)
                            .title("Constat — " + controlCode)
                            .content("Résultat : " + e.getResult().getLabel()
                                    + "\n\nConstat : " + e.getAiSummary()
                                    + (e.getImpact() != null ? "\n\nImpact : " + e.getImpact() : "")
                                    + "\n\nType de déficience : " + (e.getDeficiencyType() != null ? e.getDeficiencyType() : "N/A"))
                            .build());
                });
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse getReport(UUID reportId) {
        return toResponse(reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", reportId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getReportsByMission(UUID missionId) {
        return reportRepository.findByMissionIdOrderByGeneratedAtDesc(missionId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public ReportResponse updateSection(UUID reportId, UUID sectionId, UpdateReportSectionRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", reportId));
        if (report.getStatut() == ReportDocStatus.FINALISE) {
            throw new BusinessException("Cannot edit a finalized report");
        }
        ReportSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("ReportSection", sectionId));
        if (!section.getIsEditable()) throw new BusinessException("This section is not editable");
        section.setContent(request.getContent());
        sectionRepository.save(section);
        return toResponse(reportRepository.findById(reportId).orElseThrow());
    }

    @Override
    public void finalizeReport(UUID reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", reportId));
        report.setStatut(ReportDocStatus.FINALISE);
        reportRepository.save(report);
    }

    @Override
    public byte[] exportReportAsDocx(UUID reportId) {
        // Stub — integrate with Python agent report generator or Apache POI
        throw new BusinessException("DOCX export must be triggered via the Python AI pipeline");
    }

    private ReportResponse toResponse(Report r) {
        return ReportResponse.builder()
                .id(r.getId()).missionId(r.getMission().getId())
                .type(r.getType()).title(r.getTitle())
                .generatedAt(r.getGeneratedAt()).statut(r.getStatut())
                .filePathWord(r.getFilePathWord()).filePathPdf(r.getFilePathPdf())
                .sections(sectionRepository.findByReportIdOrderByOrderIndexAsc(r.getId()).stream()
                        .map(s -> ReportSectionResponse.builder()
                                .id(s.getId()).title(s.getTitle()).content(s.getContent())
                                .orderIndex(s.getOrderIndex()).isEditable(s.getIsEditable()).build())
                        .collect(Collectors.toList()))
                .build();
    }
}
