package com.pwc.auditit.repository;

import com.pwc.auditit.entity.TestFieldValue;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestFieldValueRepository extends MongoRepository<TestFieldValue, UUID> {
    List<TestFieldValue> findByTestResultId(UUID testResultId);
    Optional<TestFieldValue> findByTestResultIdAndControlFieldId(UUID testResultId, UUID controlFieldId);
    void deleteByTestResultId(UUID testResultId);
}
