package com.sofm.recommend.infrastructure.mongo.repository;

import com.sofm.recommend.domain.model.mongo.RecommendLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecommendLogRepository extends MongoRepository<RecommendLog,String> {

    Optional<RecommendLog> getById(String id);

    Optional<RecommendLog> getFirstByUidOrderByCreateTimeDesc(String uid);
}
