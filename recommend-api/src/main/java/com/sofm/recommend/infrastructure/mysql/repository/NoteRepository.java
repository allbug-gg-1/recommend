package com.sofm.recommend.infrastructure.mysql.repository;

import com.sofm.recommend.domain.note.entity.NoteMysqlEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface NoteRepository extends JpaRepository<NoteMysqlEntity, Integer> {

    int countByLastModifyTimeAfter(Date lastModifyTime);

    Page<NoteMysqlEntity> findByLastModifyTimeAfter(Date lastModifyTime, Pageable pageable);
}
