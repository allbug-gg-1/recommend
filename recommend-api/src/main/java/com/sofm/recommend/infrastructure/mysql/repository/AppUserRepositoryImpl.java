package com.sofm.recommend.infrastructure.mysql.repository;

import com.sofm.recommend.domain.user.entity.AppUser;
import com.sofm.recommend.domain.user.repository.AppUserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepositoryImpl extends AppUserRepository, JpaRepository<AppUser, Integer> {

    Optional<AppUser> getByRecordId(int recordId);

}
