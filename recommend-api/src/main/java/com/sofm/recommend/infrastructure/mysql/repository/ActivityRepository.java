package com.sofm.recommend.infrastructure.mysql.repository;

import com.sofm.recommend.domain.activity.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Integer> {

    int countByStatusAndFinished(String status, int finished);

    List<Activity> findByStatusAndFinished(String status, int finished);

}
