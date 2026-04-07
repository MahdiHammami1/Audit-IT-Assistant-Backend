package com.pwc.auditit.repository;

import com.pwc.auditit.entity.ItgcDomain;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItgcDomainRepository extends MongoRepository<ItgcDomain, UUID> {
    Optional<ItgcDomain> findByCode(String code);
}
