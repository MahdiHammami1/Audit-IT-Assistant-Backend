package com.pwc.auditit.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data @Builder
public class TestFieldValueResponse {
    private UUID id;
    private UUID controlFieldId;
    private String fieldLabel;
    private String valueText;
    private BigDecimal valueNumber;
    private LocalDate valueDate;
}
