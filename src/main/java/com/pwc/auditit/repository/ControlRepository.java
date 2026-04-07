package com.pwc.auditit.repository;

import com.pwc.auditit.entity.Control;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ControlRepository extends MongoRepository<Control, UUID> {
    Optional<Control> findByCode(String code);
    List<Control> findByDomainCodeOrderByOrderIndexAsc(String domainCode);
    List<Control> findByDomainIdOrderByOrderIndexAsc(UUID domainId);
}
