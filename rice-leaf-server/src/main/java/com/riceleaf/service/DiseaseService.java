package com.riceleaf.service;

import com.riceleaf.entity.DiseaseDict;
import com.riceleaf.repository.DiseaseDictRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiseaseService {

    private final DiseaseDictRepository diseaseRepo;

    public DiseaseService(DiseaseDictRepository diseaseRepo) {
        this.diseaseRepo = diseaseRepo;
    }

    public List<DiseaseDict> getAll() {
        return diseaseRepo.findAllByOrderBySortOrderAsc();
    }
}
