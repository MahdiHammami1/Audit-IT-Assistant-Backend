package com.pwc.auditit.repository;

import com.pwc.auditit.entity.UploadedFile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UploadedFileRepository extends MongoRepository<UploadedFile, UUID> {
    List<UploadedFile> findByTestResultId(UUID testResultId);
    List<UploadedFile> findByTestResultIdAndControlFieldId(UUID testResultId, UUID controlFieldId);
    void deleteByTestResultId(UUID testResultId);
}
