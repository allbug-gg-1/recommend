package com.sofm.recommend.infrastructure.mongo.repository;

import com.sofm.recommend.domain.model.mongo.PNote;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PNoteRepository extends MongoRepository<PNote,Integer> {

    Optional<PNote> getByRecordId(int recordId);

    List<PNote> findByRecordIdIn(List<Integer> recordIds);
}
