package com.sofm.recommend.infrastructure.mongo.repository;

import com.sofm.recommend.domain.note.entity.NoteMongoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteMongoRepository extends MongoRepository<NoteMongoEntity,Integer> {

    Optional<NoteMongoEntity> getByRecordId(int recordId);

    List<NoteMongoEntity> findByRecordIdIn(List<Integer> recordIds);
}
