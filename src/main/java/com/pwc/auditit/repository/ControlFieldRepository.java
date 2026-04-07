package com.pwc.auditit.repository;

import com.pwc.auditit.entity.ControlField;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ControlFieldRepository extends MongoRepository<ControlField, UUID> {
    List<ControlField> findByControlIdOrderByOrderIndexAsc(UUID controlId);
}
