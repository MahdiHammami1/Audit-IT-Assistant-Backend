package com.pwc.auditit.repository;

import com.pwc.auditit.entity.TestResult;
import com.pwc.auditit.entity.enums.TestStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestResultRepository extends MongoRepository<TestResult, UUID> {

    List<TestResult> findByMissionId(UUID missionId);

    List<TestResult> findByApplicationId(UUID applicationId);

    Optional<TestResult> findByMissionIdAndApplicationIdAndControlId(
        UUID missionId, UUID applicationId, UUID controlId);

    long countByMissionId(UUID missionId);

    long countByMissionIdAndStatut(UUID missionId, TestStatus statut);

    @Query("{ 'mission.$id': ObjectId('?0'), 'application.$id': ObjectId('?1') }")
    List<TestResult> findByMissionAndApplication(UUID missionId, UUID applicationId);
}
