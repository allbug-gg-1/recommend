package com.sofm.recommend.infrastructure.mysql.repository;

import com.sofm.recommend.domain.family.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FamilyRepository extends JpaRepository<Family,Integer> {
}
