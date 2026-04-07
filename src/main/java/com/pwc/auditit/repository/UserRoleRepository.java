package com.pwc.auditit.repository;

import com.pwc.auditit.entity.UserRole;
import com.pwc.auditit.entity.enums.AppRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends MongoRepository<UserRole, UUID> {
    List<UserRole> findByUserId(UUID userId);
    boolean existsByUserIdAndRole(UUID userId, AppRole role);
    void deleteByUserIdAndRole(UUID userId, AppRole role);
}
