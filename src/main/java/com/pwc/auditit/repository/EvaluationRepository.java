package com.pwc.auditit.repository;

import com.pwc.auditit.entity.Evaluation;
import com.pwc.auditit.entity.enums.EvaluationResult;
import com.pwc.auditit.entity.enums.ValidationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EvaluationRepository extends MongoRepository<Evaluation, UUID> {

    Optional<Evaluation> findByTestResultId(UUID testResultId);

    @Query("{ 'testResult.mission.$id': ObjectId('?0') }")
    List<Evaluation> findByMissionId(UUID missionId);

    @Query("{ 'testResult.mission.$id': ObjectId('?0'), 'result': '?1' }")
    List<Evaluation> findByMissionIdAndResult(UUID missionId, EvaluationResult result);

    long countByTestResultMissionIdAndValidationStatus(UUID missionId, ValidationStatus status);
}
