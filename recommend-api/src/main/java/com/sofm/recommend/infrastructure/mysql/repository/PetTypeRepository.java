package com.sofm.recommend.infrastructure.mysql.repository;

import com.sofm.recommend.domain.model.mysql.PetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PetTypeRepository extends JpaRepository<PetType,Integer> {
}