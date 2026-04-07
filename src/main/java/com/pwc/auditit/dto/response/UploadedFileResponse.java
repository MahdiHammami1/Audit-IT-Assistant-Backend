package com.pwc.auditit.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class UploadedFileResponse {
    private UUID id;
    private UUID controlFieldId;
    private String fileName;
    private String filePath;
    private Integer fileSize;
    private String mimeType;
    private Instant uploadedAt;
}
