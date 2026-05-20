package com.pwc.auditit.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cdws")
public class CDW {

    @Id
    private String id;

    @Indexed
    @JsonProperty("missionId")
    @Field("missionId")
    private String missionId;

    @Field("cdwNo")
    private String cdwNo;

    @JsonProperty("Description")
    @Field("Description")
    private String description;

    @JsonProperty("Remediated as of, or prior to period-end?")
    @Field("Remediated as of, or prior to period-end?")
    private Boolean remediatedAsOfOrPriorToPeriodEnd;

    @JsonProperty("Final Conclusion on Severity")
    @Field("Final Conclusion on Severity")
    private String finalConclusionOnSeverity;

    @JsonProperty("FSLI")
    @Field("FSLI")
    private String fsli;

    @JsonProperty("Internal Control Component")
    @Field("Internal Control Component")
    private String internalControlComponent;

    @JsonProperty("Business Process")
    @Field("Business Process")
    private String businessProcess;

    @JsonProperty("Audit Unit")
    @Field("Audit Unit")
    private String auditUnit;

    @JsonProperty("Exception Identified By")
    @Field("Exception Identified By")
    private String exceptionIdentifiedBy;

    @JsonProperty("Assertions")
    @Field("Assertions")
    private String assertions;

    @JsonProperty("Audit Response")
    @Field("Audit Response")
    private String auditResponse;

    @JsonProperty("Does the deficiency relate directly to the achievement of one or more financial statement assertions?")
    @Field("Does the deficiency relate directly to the achievement of one or more financial statement assertions?")
    private Boolean doesTheDeficiencyRelateDirectlyToTheAchievementOfOneOrMoreFinancialStatementAssertions;

    @JsonProperty("Is the likelihood of a misstatement resulting from the deficiency/weakness (or combination) at least reasonably possible?")
    @Field("Is the likelihood of a misstatement resulting from the deficiency/weakness (or combination) at least reasonably possible?")
    private Boolean isTheLikelihoodOfAMisstatementResultingFromTheDeficiencyWeaknessOrCombinationAtLeastReasonablyPossible;

    @JsonProperty("Value of transactions or accounts exposed or expected to be exposed to the deficiency/weakness - Interim")
    @Field("Value of transactions or accounts exposed or expected to be exposed to the deficiency/weakness - Interim")
    private BigDecimal valueOfTransactionsOrAccountsExposedOrExpectedToBeExposedToTheDeficiencyWeaknessInterim;

    @JsonProperty("Value of transactions or accounts exposed or expected to be exposed to the deficiency/weakness - Annual")
    @Field("Value of transactions or accounts exposed or expected to be exposed to the deficiency/weakness - Annual")
    private BigDecimal valueOfTransactionsOrAccountsExposedOrExpectedToBeExposedToTheDeficiencyWeaknessAnnual;

    @JsonProperty("Is the magnitude of the potential misstatement, which is at least reasonably possible (considering quantitative and qualitative factors), material to either the interim or annual financial statements?")
    @Field("Is the magnitude of the potential misstatement, which is at least reasonably possible (considering quantitative and qualitative factors), material to either the interim or annual financial statements?")
    private Boolean isTheMagnitudeOfThePotentialMisstatementMaterialToEitherTheInterimOrAnnualFinancialStatements;

    @JsonProperty("Factors considered in determining likelihood and magnitude")
    @Field("Factors considered in determining likelihood and magnitude")
    private String factorsConsideredInDeterminingLikelihoodAndMagnitude;

    @JsonProperty("Is this at least a significant deficiency?")
    @Field("Is this at least a significant deficiency?")
    private Boolean isThisAtLeastASignificantDeficiency;

    @JsonProperty("Rationale")
    @Field("Rationale")
    private String rationale;

    @JsonProperty("Do compensating controls exist and operate effectively, at a level of precision sufficient to prevent or detect a misstatement that could be material to either interim or annual financial statements?")
    @Field("Do compensating controls exist and operate effectively, at a level of precision sufficient to prevent or detect a misstatement that could be material to either interim or annual financial statements?")
    private Boolean doCompensatingControlsExistAndOperateEffectively;

    @JsonProperty("Description of Compensating Controls")
    @Field("Description of Compensating Controls")
    private String descriptionOfCompensatingControls;

    @JsonProperty("Refer to where testing (either by us or management) of compensating control was documented in the work-papers")
    @Field("Refer to where testing (either by us or management) of compensating control was documented in the work-papers")
    private String referToWhereTestingOfCompensatingControlWasDocumentedInTheWorkPapers;

    @JsonProperty("Is this a Material Weakness?")
    @Field("Is this a Material Weakness?")
    private Boolean isThisAMaterialWeakness;

    @JsonProperty("Document Rationale")
    @Field("Document Rationale")
    private String documentRationale;

    @JsonProperty("Are there multiple control deficiencies and/or significant deficiencies that affect the same financial statement account balance or disclosure?")
    @Field("Are there multiple control deficiencies and/or significant deficiencies that affect the same financial statement account balance or disclosure?")
    private Boolean areThereMultipleControlDeficienciesAffectTheSameFinancialStatementAccountBalanceOrDisclosure;

    @JsonProperty("Aggregation Description")
    @Field("Aggregation Description")
    private String aggregationDescription;

    @JsonProperty("Source Ref. No.")
    @Field("Source Ref. No.")
    private String sourceRefNo;

    @JsonProperty("Title")
    @Field("Title")
    private String title;

    @JsonProperty("Sub-process")
    @Field("Sub-process")
    private String subProcess;

    @JsonProperty("FSLI or Disclosure")
    @Field("FSLI or Disclosure")
    private String fsliOrDisclosure;

    @JsonProperty("Roll Forward?")
    @Field("Roll Forward?")
    private Boolean rollForward;

    @JsonProperty("Control No.")
    @Field("Control No.")
    private String controlNo;

    @JsonProperty("Control Title")
    @Field("Control Title")
    private String controlTitle;

    @JsonProperty("Audit Response Links")
    @Field("Audit Response Links")
    private String auditResponseLinks;

    @JsonProperty("Aggregation Reference")
    @Field("Aggregation Reference")
    private String aggregationReference;

    @JsonProperty("IsReportedToManagement")
    @Field("IsReportedToManagement")
    private Boolean isReportedToManagement;

    @JsonProperty("ExceptionAmount")
    @Field("ExceptionAmount")
    private BigDecimal exceptionAmount;

    @JsonProperty("AffectedPopulationAmount")
    @Field("AffectedPopulationAmount")
    private BigDecimal affectedPopulationAmount;

    @JsonProperty("RootCause")
    @Field("RootCause")
    private String rootCause;

    @JsonProperty("Implication")
    @Field("Implication")
    private String implication;

    @JsonProperty("Recommendation")
    @Field("Recommendation")
    private String recommendation;

    @JsonProperty("ResponsiblePerson")
    @Field("ResponsiblePerson")
    private String responsiblePerson;

    @JsonProperty("TargetDate")
    @Field("TargetDate")
    private LocalDate targetDate;

    @CreatedDate
    @JsonProperty("createdDate")
    @Field("createdDate")
    private LocalDateTime createdDate;

    @JsonProperty("createdBy")
    @Field("createdBy")
    private String createdBy;

    @LastModifiedDate
    @JsonProperty("lastModified")
    @Field("lastModified")
    private LocalDateTime lastModified;

    @JsonProperty("lastModifiedBy")
    @Field("lastModifiedBy")
    private String lastModifiedBy;
}
