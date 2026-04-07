package com.pwc.auditit.service;

import com.pwc.auditit.dto.response.ControlResponse;
import com.pwc.auditit.dto.response.ItgcDomainResponse;

import java.util.List;

public interface ItgcDomainService {
    List<ItgcDomainResponse> getAllDomains();
    List<ControlResponse> getControlsByDomain(String domainCode);
}
