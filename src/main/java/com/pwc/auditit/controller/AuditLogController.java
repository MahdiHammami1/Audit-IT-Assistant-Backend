package com.pwc.auditit.controller;

import com.pwc.auditit.dto.response.ApiResponse;
import com.pwc.auditit.dto.response.AuditLogResponse;
import com.pwc.auditit.entity.AuditLog;
import com.pwc.auditit.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Admin Audit Logs", description = "Read-only audit log endpoints for the admin console")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get audit logs", description = "Return persisted audit logs with lightweight admin-console filters")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<AuditLogResponse> logs = auditLogRepository.findAll(pageable)
                .map(this::mapLog)
                .map(log -> matches(log, action, module, user, status, from, to) ? log : null);

        Page<AuditLogResponse> filtered = new org.springframework.data.domain.PageImpl<>(
                logs.getContent().stream().filter(java.util.Objects::nonNull).toList(),
                pageable,
                logs.getTotalElements()
        );

        return ResponseEntity.ok(ApiResponse.ok("Audit logs retrieved", filtered));
    }

    private AuditLogResponse mapLog(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .timestamp(log.getCreatedAt())
                .userFullName(log.getUser() != null ? log.getUser().getFullName() : null)
                .userEmail(log.getUser() != null ? log.getUser().getEmail() : null)
                .action(log.getAction())
                .module(log.getEntityType())
                .status(resolveStatus(log.getDetails()))
                .details(log.getDetails())
                .build();
    }

    private String resolveStatus(Map<String, Object> details) {
        if (details == null) return "SUCCESS";
        Object status = details.get("status");
        return status != null ? status.toString() : "SUCCESS";
    }

    private boolean matches(AuditLogResponse log, String action, String module, String user, String status, String from, String to) {
        return contains(log.getAction(), action)
                && contains(log.getModule(), module)
                && (isBlank(user) || contains(log.getUserFullName(), user) || contains(log.getUserEmail(), user))
                && contains(log.getStatus(), status)
                && afterOrSame(log.getTimestamp(), from)
                && beforeOrSame(log.getTimestamp(), to);
    }

    private boolean contains(String value, String filter) {
        if (isBlank(filter)) return true;
        return value != null && value.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean afterOrSame(Instant timestamp, String from) {
        if (timestamp == null || isBlank(from)) return true;
        Instant start = LocalDate.parse(from).atStartOfDay().toInstant(ZoneOffset.UTC);
        return !timestamp.isBefore(start);
    }

    private boolean beforeOrSame(Instant timestamp, String to) {
        if (timestamp == null || isBlank(to)) return true;
        Instant end = LocalDate.parse(to).plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return timestamp.isBefore(end);
    }
}
