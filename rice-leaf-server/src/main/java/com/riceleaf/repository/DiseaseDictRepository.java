package com.riceleaf.repository;

import com.riceleaf.entity.DiseaseDict;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DiseaseDictRepository extends JpaRepository<DiseaseDict, Long> {
    List<DiseaseDict> findAllByOrderBySortOrderAsc();
}
