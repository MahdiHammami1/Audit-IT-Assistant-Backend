package com.pwc.auditit.service.impl;

import com.pwc.auditit.dto.response.ControlFieldResponse;
import com.pwc.auditit.dto.response.ControlResponse;
import com.pwc.auditit.dto.response.ItgcDomainResponse;
import com.pwc.auditit.exception.ResourceNotFoundException;
import com.pwc.auditit.repository.ControlFieldRepository;
import com.pwc.auditit.repository.ControlRepository;
import com.pwc.auditit.repository.ItgcDomainRepository;
import com.pwc.auditit.service.ItgcDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItgcDomainServiceImpl implements ItgcDomainService {

    private final ItgcDomainRepository domainRepository;
    private final ControlRepository controlRepository;
    private final ControlFieldRepository fieldRepository;

    @Override
    public List<ItgcDomainResponse> getAllDomains() {
        return domainRepository.findAll().stream()
                .map(d -> ItgcDomainResponse.builder()
                        .id(d.getId()).code(d.getCode())
                        .name(d.getName()).description(d.getDescription())
                        .controls(controlRepository.findByDomainIdOrderByOrderIndexAsc(d.getId()).stream()
                                .map(this::toControlResponse).collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<ControlResponse> getControlsByDomain(String domainCode) {
        domainRepository.findByCode(domainCode)
                .orElseThrow(() -> new ResourceNotFoundException("ItgcDomain", domainCode));
        return controlRepository.findByDomainCodeOrderByOrderIndexAsc(domainCode)
                .stream().map(this::toControlResponse).collect(Collectors.toList());
    }

    private ControlResponse toControlResponse(com.pwc.auditit.entity.Control c) {
        return ControlResponse.builder()
                .id(c.getId()).code(c.getCode()).domainCode(c.getDomain().getCode())
                .title(c.getTitle()).description(c.getDescription()).orderIndex(c.getOrderIndex())
                .fields(fieldRepository.findByControlIdOrderByOrderIndexAsc(c.getId()).stream()
                        .map(f -> ControlFieldResponse.builder()
                                .id(f.getId()).label(f.getLabel())
                                .fieldType(f.getFieldType()).isRequired(f.getIsRequired())
                                .orderIndex(f.getOrderIndex()).build())
                        .collect(Collectors.toList()))
                .build();
    }
}
