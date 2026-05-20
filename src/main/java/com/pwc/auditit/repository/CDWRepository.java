package com.pwc.auditit.repository;

import com.pwc.auditit.entity.CDW;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CDWRepository extends MongoRepository<CDW, String> {
    
    List<CDW> findByMissionId(String missionId);
    
    Page<CDW> findByMissionId(String missionId, Pageable pageable);
    
    Optional<CDW> findByIdAndMissionId(String id, String missionId);
    
    List<CDW> findAllByIdInAndMissionId(List<String> ids, String missionId);
    
    
    Long countByMissionId(String missionId);
    
    @Query("{ 'missionId' : ?0, $or: [ { 'CD/W No.' : { $regex: ?1, $options: 'i' } }, { 'Title' : { $regex: ?1, $options: 'i' } } ] }")
    List<CDW> searchByMissionId(String missionId, String search);
    
    void deleteByIdAndMissionId(String id, String missionId);
    
    long deleteByMissionId(String missionId);
}
