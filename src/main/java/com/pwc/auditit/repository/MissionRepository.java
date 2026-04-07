package com.pwc.auditit.repository;

import com.pwc.auditit.entity.Mission;
import com.pwc.auditit.entity.enums.MissionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MissionRepository extends MongoRepository<Mission, UUID> {

    List<Mission> findByAuditeurResponsableId(UUID auditorId);

    List<Mission> findByStatut(MissionStatus statut);

    @Query("{ $or: [ { 'auditeurResponsable.$id': ObjectId('?0') }, { 'teamMembers': { $elemMatch: { 'user.$id': ObjectId('?0') } } } ] }")
    List<Mission> findAccessibleByUser(UUID userId);

    @Query("{ $and: [ { $or: [ { 'societe': { $regex: '?0', $options: 'i' } }, { 'societe': { $exists: false } } ] }, { $or: [ { 'exercice': '?1' }, { 'exercice': { $exists: false } } ] }, { $or: [ { 'statut': '?2' }, { 'statut': { $exists: false } } ] } ] }")
    List<Mission> findWithFilters(String societe, String exercice, MissionStatus statut);
}
