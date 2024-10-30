package com.sofm.recommend.domain.user.repository;

import com.sofm.recommend.domain.user.entity.AppUser;

import java.util.Optional;

public interface AppUserRepository {

    Optional<AppUser> getByRecordId(int recordId);
}
