package com.sofm.recommend.infrastructure.mysql.repository;

import com.sofm.recommend.domain.pet.entity.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface PetRepository extends JpaRepository<Pet, Integer> {

    Page<Pet> findByLastModifyTimeAfter(Date lastModifyTime, Pageable pageable);

    int countByLastModifyTimeAfter(Date lastModifyTime);

    @Query(value = "select p.type from Pet p where p.recordId = :recordId")
    int getTypeByRecordId(int recordId);

}
