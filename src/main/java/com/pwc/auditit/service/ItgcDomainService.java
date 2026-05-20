package com.pwc.auditit.service;

import com.pwc.auditit.dto.request.BulkCreateControlRequest;
import com.pwc.auditit.dto.request.CreateItgcDomainRequest;
import com.pwc.auditit.dto.response.ControlResponse;
import com.pwc.auditit.dto.response.ItgcDomainResponse;

import java.util.List;
import java.util.UUID;

public interface ItgcDomainService {
    List<ItgcDomainResponse> getAllDomains();
    List<ItgcDomainResponse> createDomains(List<CreateItgcDomainRequest> requests);
    List<ControlResponse> getControlsByDomain(String domainCode);
    List<ControlResponse> createControlsForDomain(BulkCreateControlRequest request);
    void deleteControl(UUID controlId);
    long deleteAll();
}
