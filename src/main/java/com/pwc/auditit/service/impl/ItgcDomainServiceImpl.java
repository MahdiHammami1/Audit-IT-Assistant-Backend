package com.pwc.auditit.service.impl;

import com.pwc.auditit.dto.request.BulkCreateControlRequest;
import com.pwc.auditit.dto.request.CreateControlRequest;
import com.pwc.auditit.dto.request.CreateItgcDomainRequest;
import com.pwc.auditit.dto.response.ControlFieldResponse;
import com.pwc.auditit.dto.response.ControlResponse;
import com.pwc.auditit.dto.response.ItgcDomainResponse;
import com.pwc.auditit.entity.Control;
import com.pwc.auditit.entity.ItgcDomain;
import com.pwc.auditit.exception.ResourceNotFoundException;
import com.pwc.auditit.repository.ControlFieldRepository;
import com.pwc.auditit.repository.ControlRepository;
import com.pwc.auditit.repository.ItgcDomainRepository;
import com.pwc.auditit.service.ItgcDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
    @Transactional
    public List<ItgcDomainResponse> createDomains(List<CreateItgcDomainRequest> requests) {
        return requests.stream()
                .map(request -> {
                    ItgcDomain domain = ItgcDomain.builder()
                            .id(UUID.randomUUID())
                            .code(request.getCode())
                            .name(request.getName())
                            .description(request.getDescription())
                            .build();
                    ItgcDomain savedDomain = domainRepository.save(domain);
                    return ItgcDomainResponse.builder()
                            .id(savedDomain.getId())
                            .code(savedDomain.getCode())
                            .name(savedDomain.getName())
                            .description(savedDomain.getDescription())
                            .controls(List.of())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ControlResponse> getControlsByDomain(String domainCode) {
        ItgcDomain domain = domainRepository.findByCode(domainCode)
                .orElseThrow(() -> new ResourceNotFoundException("ItgcDomain", domainCode));
        return controlRepository.findByDomainIdOrderByOrderIndexAsc(domain.getId())
                .stream().map(this::toControlResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ControlResponse> createControlsForDomain(BulkCreateControlRequest request) {
        ItgcDomain domain = domainRepository.findByCode(request.getDomainCode())
                .orElseThrow(() -> new ResourceNotFoundException("ItgcDomain", request.getDomainCode()));

        List<Control> createdControls = new ArrayList<>();
        for (CreateControlRequest controlRequest : request.getControls()) {
            Control control = Control.builder()
                    .id(UUID.randomUUID())
                    .domain(domain)
                    .code(controlRequest.getCode())
                    .title(controlRequest.getTitle())
                    .description(controlRequest.getDescription())
                    .orderIndex(controlRequest.getOrderIndex() != null ? controlRequest.getOrderIndex() : 0)
                    .fields(new ArrayList<>())
                    .build();
            Control savedControl = controlRepository.save(control);
            createdControls.add(savedControl);
        }

        return createdControls.stream().map(this::toControlResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public long deleteAll() {
        long count = domainRepository.count();
        domainRepository.deleteAll();
        return count;
    }

    @Override
    @Transactional
    public void deleteControl(UUID controlId) {
        if (!controlRepository.existsById(controlId)) {
            throw new ResourceNotFoundException("Control", controlId);
        }
        controlRepository.deleteById(controlId);
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
