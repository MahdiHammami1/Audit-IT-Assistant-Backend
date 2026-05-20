package com.pwc.auditit.service;

import com.pwc.auditit.dto.request.CDWCreateRequest;
import com.pwc.auditit.dto.request.CDWUpdateRequest;
import com.pwc.auditit.dto.response.CDWListItem;
import com.pwc.auditit.dto.response.CDWResponse;
import com.pwc.auditit.entity.CDW;
import com.pwc.auditit.exception.ResourceNotFoundException;
import com.pwc.auditit.repository.CDWRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.util.StringUtils.hasText;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CDWService {
    
    private final CDWRepository cdwRepository;
    
    // ==================== CREATE ====================
    public CDWResponse createCDW(CDWCreateRequest request, String currentUser) {
        validateCDWNo(request.getCdwNo());
        
        CDW cdw = new CDW();
        mapRequestToCDW(request, cdw);
        
        // Double validation - ensure cdwNo is never null after mapping
        if (!hasText(cdw.getCdwNo())) {
            throw new IllegalArgumentException("CD/W No. is required and cannot be null or blank");
        }
        
        cdw.setMissionId(request.getMissionId());
        cdw.setCreatedBy(currentUser);
        cdw.setLastModifiedBy(currentUser);
        
        CDW savedCDW = cdwRepository.save(cdw);
        log.info("CDW created: {} for mission: {}", savedCDW.getId(), request.getMissionId());
        return mapCDWToResponse(savedCDW);
    }
    
    public List<CDWResponse> createCDWsBulk(List<CDWCreateRequest> requests, String missionId, String currentUser) {
        return requests.stream()
            .map(request -> {
                validateCDWNo(request.getCdwNo());
                
                CDW cdw = new CDW();
                mapRequestToCDW(request, cdw);
                
                // Double validation - ensure cdwNo is never null after mapping
                if (!hasText(cdw.getCdwNo())) {
                    throw new IllegalArgumentException("CD/W No. is required and cannot be null or blank");
                }
                
                cdw.setMissionId(missionId);
                cdw.setCreatedBy(currentUser);
                cdw.setLastModifiedBy(currentUser);
                
                CDW savedCDW = cdwRepository.save(cdw);
                log.info("CDW created in bulk: {} for mission: {}", savedCDW.getId(), missionId);
                return mapCDWToResponse(savedCDW);
            })
            .collect(Collectors.toList());
    }
    
     // ==================== READ ====================
    // ==================== READ ====================
    public CDWResponse getCDWById(String id, String missionId) {
        CDW cdw = cdwRepository.findByIdAndMissionId(id, missionId)
            .orElseThrow(() -> new ResourceNotFoundException("CDW not found with id: " + id));
        return mapCDWToResponse(cdw);
    }

    public Page<CDWListItem> getCDWsByMission(String missionId, Pageable pageable) {
        return cdwRepository.findByMissionId(missionId, pageable)
            .map(this::mapCDWToListItem);
    }

    public List<CDWResponse> getCDWsByIds(List<String> ids, String missionId) {
        return cdwRepository.findAllByIdInAndMissionId(ids, missionId)
            .stream()
            .map(this::mapCDWToResponse)
            .collect(Collectors.toList());
    }
    
    public Page<CDWListItem> getCDWsByMissionWithFilters(
            String missionId, 
            String status, 
            String severity, 
            Pageable pageable) {
        // Simple implementation - returns all CDWs for mission
        // Filtering can be enhanced as needed
        return cdwRepository.findByMissionId(missionId, pageable)
            .map(this::mapCDWToListItem);
    }
    
    // ==================== SEARCH ====================
    public List<CDWListItem> searchCDWs(String missionId, String search) {
        return cdwRepository.searchByMissionId(missionId, search)
            .stream()
            .map(this::mapCDWToListItem)
            .collect(Collectors.toList());
    }
    
    // ==================== UPDATE ====================
    public CDWResponse updateCDW(String id, String missionId, CDWUpdateRequest request, String currentUser) {
        CDW cdw = cdwRepository.findByIdAndMissionId(id, missionId)
            .orElseThrow(() -> new ResourceNotFoundException("CDW not found with id: " + id));
        
        mapUpdateRequestToCDW(request, cdw);
        cdw.setLastModifiedBy(currentUser);
        
        CDW updatedCDW = cdwRepository.save(cdw);
        log.info("CDW updated: {} for mission: {}", id, missionId);
        return mapCDWToResponse(updatedCDW);
    }
    
    // ==================== DELETE ====================
    public void deleteCDW(String id, String missionId) {
        cdwRepository.deleteByIdAndMissionId(id, missionId);
        log.info("CDW deleted: {} for mission: {}", id, missionId);
    }
    
    public void deleteCDWs(List<String> ids, String missionId) {
        ids.forEach(id -> cdwRepository.deleteByIdAndMissionId(id, missionId));
        log.info("Deleted {} CDWs for mission: {}", ids.size(), missionId);
    }
    
    public long deleteAllByMission(String missionId) {
        long deletedCount = cdwRepository.deleteByMissionId(missionId);
        log.info("Deleted {} CDWs for mission: {}", deletedCount, missionId);
        return deletedCount;
    }
    
    // ==================== STATISTICS ====================
    public long getCDWCountByMission(String missionId) {
        return cdwRepository.countByMissionId(missionId);
    }
    
    // ==================== MAPPING METHODS ====================
    
    private void mapRequestToCDW(CDWCreateRequest request, CDW cdw) {
        cdw.setCdwNo(request.getCdwNo());
        cdw.setDescription(request.getDescription());
        cdw.setRemediatedAsOfOrPriorToPeriodEnd(request.getRemediatedAsOfOrPriorToPeriodEnd());
        cdw.setFinalConclusionOnSeverity(request.getFinalConclusionOnSeverity());
        cdw.setFsli(request.getFsli());
        cdw.setInternalControlComponent(request.getInternalControlComponent());
        cdw.setBusinessProcess(request.getBusinessProcess());
        cdw.setAuditUnit(request.getAuditUnit());
        cdw.setExceptionIdentifiedBy(request.getExceptionIdentifiedBy());
        cdw.setAssertions(request.getAssertions());
        cdw.setAuditResponse(request.getAuditResponse());
        cdw.setDoesTheDeficiencyRelateDirectlyToTheAchievementOfOneOrMoreFinancialStatementAssertions(
            request.getDoesTheDeficiencyRelateDirectlyToTheAchievementOfOneOrMoreFinancialStatementAssertions()
        );
        cdw.setIsTheLikelihoodOfAMisstatementResultingFromTheDeficiencyWeaknessOrCombinationAtLeastReasonablyPossible(
            request.getIsTheLikelihoodOfAMisstatementResultingFromTheDeficiencyWeaknessOrCombinationAtLeastReasonablyPossible()
        );
        cdw.setValueOfTransactionsOrAccountsExposedOrExpectedToBeExposedToTheDeficiencyWeaknessInterim(
            request.getValueOfTransactionsOrAccountsExposedOrExpectedToBeExposedToTheDeficiencyWeaknessInterim()
        );
        cdw.setValueOfTransactionsOrAccountsExposedOrExpectedToBeExposedToTheDeficiencyWeaknessAnnual(
            request.getValueOfTransactionsOrAccountsExposedOrExpectedToBeExposedToTheDeficiencyWeaknessAnnual()
        );
        cdw.setIsTheMagnitudeOfThePotentialMisstatementMaterialToEitherTheInterimOrAnnualFinancialStatements(
            request.getIsTheMagnitudeOfThePotentialMisstatementMaterialToEitherTheInterimOrAnnualFinancialStatements()
        );
        cdw.setFactorsConsideredInDeterminingLikelihoodAndMagnitude(
            request.getFactorsConsideredInDeterminingLikelihoodAndMagnitude()
        );
        cdw.setIsThisAtLeastASignificantDeficiency(request.getIsThisAtLeastASignificantDeficiency());
        cdw.setRationale(request.getRationale());
        cdw.setDoCompensatingControlsExistAndOperateEffectively(request.getDoCompensatingControlsExistAndOperateEffectively());
        cdw.setDescriptionOfCompensatingControls(request.getDescriptionOfCompensatingControls());
        cdw.setReferToWhereTestingOfCompensatingControlWasDocumentedInTheWorkPapers(
            request.getReferToWhereTestingOfCompensatingControlWasDocumentedInTheWorkPapers()
        );
        cdw.setIsThisAMaterialWeakness(request.getIsThisAMaterialWeakness());
        cdw.setDocumentRationale(request.getDocumentRationale());
        cdw.setAreThereMultipleControlDeficienciesAffectTheSameFinancialStatementAccountBalanceOrDisclosure(
            request.getAreThereMultipleControlDeficienciesAffectTheSameFinancialStatementAccountBalanceOrDisclosure()
        );
        cdw.setAggregationDescription(request.getAggregationDescription());
        cdw.setSourceRefNo(request.getSourceRefNo());
        cdw.setTitle(request.getTitle());
        cdw.setSubProcess(request.getSubProcess());
        cdw.setFsliOrDisclosure(request.getFsliOrDisclosure());
        cdw.setRollForward(request.getRollForward());
        cdw.setControlNo(request.getControlNo());
        cdw.setControlTitle(request.getControlTitle());
        cdw.setAuditResponseLinks(request.getAuditResponseLinks());
        cdw.setAggregationReference(request.getAggregationReference());
        cdw.setIsReportedToManagement(request.getIsReportedToManagement());
        cdw.setExceptionAmount(request.getExceptionAmount());
        cdw.setAffectedPopulationAmount(request.getAffectedPopulationAmount());
        cdw.setRootCause(request.getRootCause());
        cdw.setImplication(request.getImplication());
        cdw.setRecommendation(request.getRecommendation());
        cdw.setResponsiblePerson(request.getResponsiblePerson());
        cdw.setTargetDate(request.getTargetDate());
    }
    
    private void mapUpdateRequestToCDW(CDWUpdateRequest request, CDW cdw) {
        if (request.getCdwNo() != null) cdw.setCdwNo(request.getCdwNo());
        if (request.getDescription() != null) cdw.setDescription(request.getDescription());
        if (request.getRemediatedAsOfOrPriorToPeriodEnd() != null) cdw.setRemediatedAsOfOrPriorToPeriodEnd(request.getRemediatedAsOfOrPriorToPeriodEnd());
        if (request.getFinalConclusionOnSeverity() != null) cdw.setFinalConclusionOnSeverity(request.getFinalConclusionOnSeverity());
        if (request.getFsli() != null) cdw.setFsli(request.getFsli());
        if (request.getInternalControlComponent() != null) cdw.setInternalControlComponent(request.getInternalControlComponent());
        if (request.getBusinessProcess() != null) cdw.setBusinessProcess(request.getBusinessProcess());
        if (request.getAuditUnit() != null) cdw.setAuditUnit(request.getAuditUnit());
        if (request.getExceptionIdentifiedBy() != null) cdw.setExceptionIdentifiedBy(request.getExceptionIdentifiedBy());
        if (request.getAssertions() != null) cdw.setAssertions(request.getAssertions());
        if (request.getAuditResponse() != null) cdw.setAuditResponse(request.getAuditResponse());
        if (request.getDoesTheDeficiencyRelateDirectlyToTheAchievementOfOneOrMoreFinancialStatementAssertions() != null)
            cdw.setDoesTheDeficiencyRelateDirectlyToTheAchievementOfOneOrMoreFinancialStatementAssertions(
                request.getDoesTheDeficiencyRelateDirectlyToTheAchievementOfOneOrMoreFinancialStatementAssertions());
        if (request.getIsTheLikelihoodOfAMisstatementResultingFromTheDeficiencyWeaknessOrCombinationAtLeastReasonablyPossible() != null)
            cdw.setIsTheLikelihoodOfAMisstatementResultingFromTheDeficiencyWeaknessOrCombinationAtLeastReasonablyPossible(
                request.getIsTheLikelihoodOfAMisstatementResultingFromTheDeficiencyWeaknessOrCombinationAtLeastReasonablyPossible());
        if (request.getValueOfTransactionsOrAccountsExposedOrExpectedToBeExposedToTheDeficiencyWeaknessInterim() != null)
            cdw.setValueOfTransactionsOrAccountsExposedOrExpectedToBeExposedToTheDeficiencyWeaknessInterim(
                request.getValueOfTransactionsOrAccountsExposedOrExpectedToBeExposedToTheDeficiencyWeaknessInterim());
        if (request.getValueOfTransactionsOrAccountsExposedOrExpectedToBeExposedToTheDeficiencyWeaknessAnnual() != null)
            cdw.setValueOfTransactionsOrAccountsExposedOrExpectedToBeExposedToTheDeficiencyWeaknessAnnual(
                request.getValueOfTransactionsOrAccountsExposedOrExpectedToBeExposedToTheDeficiencyWeaknessAnnual());
        if (request.getIsTheMagnitudeOfThePotentialMisstatementMaterialToEitherTheInterimOrAnnualFinancialStatements() != null)
            cdw.setIsTheMagnitudeOfThePotentialMisstatementMaterialToEitherTheInterimOrAnnualFinancialStatements(
                request.getIsTheMagnitudeOfThePotentialMisstatementMaterialToEitherTheInterimOrAnnualFinancialStatements());
        if (request.getFactorsConsideredInDeterminingLikelihoodAndMagnitude() != null)
            cdw.setFactorsConsideredInDeterminingLikelihoodAndMagnitude(request.getFactorsConsideredInDeterminingLikelihoodAndMagnitude());
        if (request.getIsThisAtLeastASignificantDeficiency() != null) cdw.setIsThisAtLeastASignificantDeficiency(request.getIsThisAtLeastASignificantDeficiency());
        if (request.getRationale() != null) cdw.setRationale(request.getRationale());
        if (request.getDoCompensatingControlsExistAndOperateEffectively() != null)
            cdw.setDoCompensatingControlsExistAndOperateEffectively(request.getDoCompensatingControlsExistAndOperateEffectively());
        if (request.getDescriptionOfCompensatingControls() != null) cdw.setDescriptionOfCompensatingControls(request.getDescriptionOfCompensatingControls());
        if (request.getReferToWhereTestingOfCompensatingControlWasDocumentedInTheWorkPapers() != null)
            cdw.setReferToWhereTestingOfCompensatingControlWasDocumentedInTheWorkPapers(
                request.getReferToWhereTestingOfCompensatingControlWasDocumentedInTheWorkPapers());
        if (request.getIsThisAMaterialWeakness() != null) cdw.setIsThisAMaterialWeakness(request.getIsThisAMaterialWeakness());
        if (request.getDocumentRationale() != null) cdw.setDocumentRationale(request.getDocumentRationale());
        if (request.getAreThereMultipleControlDeficienciesAffectTheSameFinancialStatementAccountBalanceOrDisclosure() != null)
            cdw.setAreThereMultipleControlDeficienciesAffectTheSameFinancialStatementAccountBalanceOrDisclosure(
                request.getAreThereMultipleControlDeficienciesAffectTheSameFinancialStatementAccountBalanceOrDisclosure());
        if (request.getAggregationDescription() != null) cdw.setAggregationDescription(request.getAggregationDescription());
        if (request.getSourceRefNo() != null) cdw.setSourceRefNo(request.getSourceRefNo());
        if (request.getTitle() != null) cdw.setTitle(request.getTitle());
        if (request.getSubProcess() != null) cdw.setSubProcess(request.getSubProcess());
        if (request.getFsliOrDisclosure() != null) cdw.setFsliOrDisclosure(request.getFsliOrDisclosure());
        if (request.getRollForward() != null) cdw.setRollForward(request.getRollForward());
        if (request.getControlNo() != null) cdw.setControlNo(request.getControlNo());
        if (request.getControlTitle() != null) cdw.setControlTitle(request.getControlTitle());
        if (request.getAuditResponseLinks() != null) cdw.setAuditResponseLinks(request.getAuditResponseLinks());
        if (request.getAggregationReference() != null) cdw.setAggregationReference(request.getAggregationReference());
        if (request.getIsReportedToManagement() != null) cdw.setIsReportedToManagement(request.getIsReportedToManagement());
        if (request.getExceptionAmount() != null) cdw.setExceptionAmount(request.getExceptionAmount());
        if (request.getAffectedPopulationAmount() != null) cdw.setAffectedPopulationAmount(request.getAffectedPopulationAmount());
        if (request.getRootCause() != null) cdw.setRootCause(request.getRootCause());
        if (request.getImplication() != null) cdw.setImplication(request.getImplication());
        if (request.getRecommendation() != null) cdw.setRecommendation(request.getRecommendation());
        if (request.getResponsiblePerson() != null) cdw.setResponsiblePerson(request.getResponsiblePerson());
        if (request.getTargetDate() != null) cdw.setTargetDate(request.getTargetDate());
    }
    
    private CDWResponse mapCDWToResponse(CDW cdw) {
        CDWResponse response = new CDWResponse();
        response.setId(cdw.getId());
        response.setMissionId(cdw.getMissionId());
        response.setCdwNo(cdw.getCdwNo());
        response.setDescription(cdw.getDescription());
        response.setRemediatedAsOfOrPriorToPeriodEnd(cdw.getRemediatedAsOfOrPriorToPeriodEnd());
        response.setFinalConclusionOnSeverity(cdw.getFinalConclusionOnSeverity());
        response.setFsli(cdw.getFsli());
        response.setInternalControlComponent(cdw.getInternalControlComponent());
        response.setBusinessProcess(cdw.getBusinessProcess());
        response.setAuditUnit(cdw.getAuditUnit());
        response.setExceptionIdentifiedBy(cdw.getExceptionIdentifiedBy());
        response.setAssertions(cdw.getAssertions());
        response.setAuditResponse(cdw.getAuditResponse());
        response.setDoesTheDeficiencyRelateDirectlyToTheAchievementOfOneOrMoreFinancialStatementAssertions(
            cdw.getDoesTheDeficiencyRelateDirectlyToTheAchievementOfOneOrMoreFinancialStatementAssertions());
        response.setIsTheLikelihoodOfAMisstatementResultingFromTheDeficiencyWeaknessOrCombinationAtLeastReasonablyPossible(
            cdw.getIsTheLikelihoodOfAMisstatementResultingFromTheDeficiencyWeaknessOrCombinationAtLeastReasonablyPossible());
        response.setValueOfTransactionsOrAccountsExposedOrExpectedToBeExposedToTheDeficiencyWeaknessInterim(
            cdw.getValueOfTransactionsOrAccountsExposedOrExpectedToBeExposedToTheDeficiencyWeaknessInterim());
        response.setValueOfTransactionsOrAccountsExposedOrExpectedToBeExposedToTheDeficiencyWeaknessAnnual(
            cdw.getValueOfTransactionsOrAccountsExposedOrExpectedToBeExposedToTheDeficiencyWeaknessAnnual());
        response.setIsTheMagnitudeOfThePotentialMisstatementMaterialToEitherTheInterimOrAnnualFinancialStatements(
            cdw.getIsTheMagnitudeOfThePotentialMisstatementMaterialToEitherTheInterimOrAnnualFinancialStatements());
        response.setFactorsConsideredInDeterminingLikelihoodAndMagnitude(cdw.getFactorsConsideredInDeterminingLikelihoodAndMagnitude());
        response.setIsThisAtLeastASignificantDeficiency(cdw.getIsThisAtLeastASignificantDeficiency());
        response.setRationale(cdw.getRationale());
        response.setDoCompensatingControlsExistAndOperateEffectively(cdw.getDoCompensatingControlsExistAndOperateEffectively());
        response.setDescriptionOfCompensatingControls(cdw.getDescriptionOfCompensatingControls());
        response.setReferToWhereTestingOfCompensatingControlWasDocumentedInTheWorkPapers(
            cdw.getReferToWhereTestingOfCompensatingControlWasDocumentedInTheWorkPapers());
        response.setIsThisAMaterialWeakness(cdw.getIsThisAMaterialWeakness());
        response.setDocumentRationale(cdw.getDocumentRationale());
        response.setAreThereMultipleControlDeficienciesAffectTheSameFinancialStatementAccountBalanceOrDisclosure(
            cdw.getAreThereMultipleControlDeficienciesAffectTheSameFinancialStatementAccountBalanceOrDisclosure());
        response.setAggregationDescription(cdw.getAggregationDescription());
        response.setSourceRefNo(cdw.getSourceRefNo());
        response.setTitle(cdw.getTitle());
        response.setSubProcess(cdw.getSubProcess());
        response.setFsliOrDisclosure(cdw.getFsliOrDisclosure());
        response.setRollForward(cdw.getRollForward());
        response.setControlNo(cdw.getControlNo());
        response.setControlTitle(cdw.getControlTitle());
        response.setAuditResponseLinks(cdw.getAuditResponseLinks());
        response.setAggregationReference(cdw.getAggregationReference());
        response.setIsReportedToManagement(cdw.getIsReportedToManagement());
        response.setExceptionAmount(cdw.getExceptionAmount());
        response.setAffectedPopulationAmount(cdw.getAffectedPopulationAmount());
        response.setRootCause(cdw.getRootCause());
        response.setImplication(cdw.getImplication());
        response.setRecommendation(cdw.getRecommendation());
        response.setResponsiblePerson(cdw.getResponsiblePerson());
        response.setTargetDate(cdw.getTargetDate());
        response.setCreatedDate(cdw.getCreatedDate());
        response.setCreatedBy(cdw.getCreatedBy());
        response.setLastModified(cdw.getLastModified());
        response.setLastModifiedBy(cdw.getLastModifiedBy());
        return response;
    }
    
    private CDWListItem mapCDWToListItem(CDW cdw) {
        return new CDWListItem(
            cdw.getId(),
            cdw.getCdwNo(),
            cdw.getTitle(),
            cdw.getFinalConclusionOnSeverity(),
            null, // status removed
            cdw.getAuditUnit(),
            cdw.getResponsiblePerson(),
            cdw.getTargetDate(),
            cdw.getCreatedDate(),
            cdw.getCreatedBy()
        );
    }
    
    // ==================== VALIDATION ====================
    
    private void validateCDWNo(String cdwNo) {
        if (!hasText(cdwNo)) {
            throw new IllegalArgumentException("CD/W No. cannot be blank or null");
        }
    }
}
