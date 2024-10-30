package com.sofm.recommend.domain.family.service;

import com.sofm.recommend.infrastructure.mysql.repository.FamilyRepository;
import org.springframework.stereotype.Service;

@Service
public class FamilyService {

    private final FamilyRepository familyRepository;

    public FamilyService(FamilyRepository familyRepository) {
        this.familyRepository = familyRepository;
    }
}
