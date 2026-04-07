package com.pwc.auditit.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class SaveTestFieldValuesRequest {
    private List<FieldValueEntry> values;

    @Data
    public static class FieldValueEntry {
        private UUID controlFieldId;
        private String valueText;
        private BigDecimal valueNumber;
        private LocalDate valueDate;
    }
}
