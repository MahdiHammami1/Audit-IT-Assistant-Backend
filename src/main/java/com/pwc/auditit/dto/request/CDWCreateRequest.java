package com.pwc.auditit.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CDWCreateRequest {
    private String missionId;

    @JsonProperty("CD/W No.")
    @NotBlank(message = "CD/W No. cannot be blank")
    private String cdwNo;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("Remediated as of, or prior to period-end?")
    private Boolean remediatedAsOfOrPriorToPeriodEnd;

    @JsonProperty("Final Conclusion on Severity")
    private String finalConclusionOnSeverity;

    @JsonProperty("FSLI")
    private String fsli;

    @JsonProperty("Internal Control Component")
    private String internalControlComponent;

    @JsonProperty("Business Process")
    private String businessProcess;

    @JsonProperty("Audit Unit")
    private String auditUnit;

    @JsonProperty("Exception Identified By")
    private String exceptionIdentifiedBy;

    @JsonProperty("Assertions")
    private String assertions;

    @JsonProperty("Audit Response")
    private String auditResponse;

    @JsonProperty("Does the deficiency relate directly to the achievement of one or more financial statement assertions?")
    private Boolean doesTheDeficiencyRelateDirectlyToTheAchievementOfOneOrMoreFinancialStatementAssertions;

    @JsonProperty("Is the likelihood of a misstatement resulting from the deficiency/weakness (or combination) at least reasonably possible?")
    private Boolean isTheLikelihoodOfAMisstatementResultingFromTheDeficiencyWeaknessOrCombinationAtLeastReasonablyPossible;

    @JsonProperty("Value of transactions or accounts exposed or expected to be exposed to the deficiency/weakness - Interim")
    private BigDecimal valueOfTransactionsOrAccountsExposedOrExpectedToBeExposedToTheDeficiencyWeaknessInterim;

    @JsonProperty("Value of transactions or accounts exposed or expected to be exposed to the deficiency/weakness - Annual")
    private BigDecimal valueOfTransactionsOrAccountsExposedOrExpectedToBeExposedToTheDeficiencyWeaknessAnnual;

    @JsonProperty("Is the magnitude of the potential misstatement, which is at least reasonably possible (considering quantitative and qualitative factors), material to either the interim or annual financial statements?")
    private Boolean isTheMagnitudeOfThePotentialMisstatementMaterialToEitherTheInterimOrAnnualFinancialStatements;

    @JsonProperty("Factors considered in determining likelihood and magnitude")
    private String factorsConsideredInDeterminingLikelihoodAndMagnitude;

    @JsonProperty("Is this at least a significant deficiency?")
    private Boolean isThisAtLeastASignificantDeficiency;

    @JsonProperty("Rationale")
    private String rationale;

    @JsonProperty("Do compensating controls exist and operate effectively, at a level of precision sufficient to prevent or detect a misstatement that could be material to either interim or annual financial statements?")
    private Boolean doCompensatingControlsExistAndOperateEffectively;

    @JsonProperty("Description of Compensating Controls")
    private String descriptionOfCompensatingControls;

    @JsonProperty("Refer to where testing (either by us or management) of compensating control was documented in the work-papers")
    private String referToWhereTestingOfCompensatingControlWasDocumentedInTheWorkPapers;

    @JsonProperty("Is this a Material Weakness?")
    private Boolean isThisAMaterialWeakness;

    @JsonProperty("Document Rationale")
    private String documentRationale;

    @JsonProperty("Are there multiple control deficiencies and/or significant deficiencies that affect the same financial statement account balance or disclosure?")
    private Boolean areThereMultipleControlDeficienciesAffectTheSameFinancialStatementAccountBalanceOrDisclosure;

    @JsonProperty("Aggregation Description")
    private String aggregationDescription;

    @JsonProperty("Source Ref. No.")
    private String sourceRefNo;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Sub-process")
    private String subProcess;

    @JsonProperty("FSLI or Disclosure")
    private String fsliOrDisclosure;

    @JsonProperty("Roll Forward?")
    private Boolean rollForward;

    @JsonProperty("Control No.")
    private String controlNo;

    @JsonProperty("Control Title")
    private String controlTitle;

    @JsonProperty("Audit Response Links")
    private String auditResponseLinks;

    @JsonProperty("Aggregation Reference")
    private String aggregationReference;

    @JsonProperty("IsReportedToManagement")
    private Boolean isReportedToManagement;

    @JsonProperty("ExceptionAmount")
    private BigDecimal exceptionAmount;

    @JsonProperty("AffectedPopulationAmount")
    private BigDecimal affectedPopulationAmount;

    @JsonProperty("RootCause")
    private String rootCause;

    @JsonProperty("Implication")
    private String implication;

    @JsonProperty("Recommendation")
    private String recommendation;

    @JsonProperty("ResponsiblePerson")
    private String responsiblePerson;

    @JsonProperty("TargetDate")
    private LocalDate targetDate;
}
