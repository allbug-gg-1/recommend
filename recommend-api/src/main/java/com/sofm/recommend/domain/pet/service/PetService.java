package com.sofm.recommend.domain.pet.service;

import com.sofm.recommend.domain.pet.entity.Pet;
import com.sofm.recommend.infrastructure.mysql.repository.PetRepository;
import com.sofm.recommend.infrastructure.mysql.repository.PetTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class PetService {

    private final PetRepository petRepository;
    private final PetTypeRepository petTypeRepository;

    public PetService(PetRepository petRepository, PetTypeRepository petTypeRepository) {
        this.petRepository = petRepository;
        this.petTypeRepository = petTypeRepository;
    }

    @Transactional(readOnly = true)
    public Page<Pet> loadLastModifyPet(Date lastTime, int page, int pageSize) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize);
        return petRepository.findByLastModifyTimeAfter(lastTime, pageable);
    }

    @Transactional(readOnly = true)
    public int countLastModifyPet(Date lastTime) {
        return petRepository.countByLastModifyTimeAfter(lastTime);
    }


    @Transactional(readOnly = true)
    public int getTypeByRecordId(int recordId) {
        return petRepository.getTypeByRecordId(recordId);
    }
}
