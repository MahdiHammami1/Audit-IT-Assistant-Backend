package com.pwc.auditit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CDWListItem {
    private String id;
    private String cdwNo;
    private String title;
    private String severity;
    private String status;
    private String auditUnit;
    private String responsiblePerson;
    private LocalDate targetDate;
    private LocalDateTime createdDate;
    private String createdBy;
}
