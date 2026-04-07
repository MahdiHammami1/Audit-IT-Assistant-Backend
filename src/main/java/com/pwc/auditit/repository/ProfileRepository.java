package com.pwc.auditit.repository;

import com.pwc.auditit.entity.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends MongoRepository<Profile, UUID> {
    Optional<Profile> findByEmail(String email);
    boolean existsByEmail(String email);
}
