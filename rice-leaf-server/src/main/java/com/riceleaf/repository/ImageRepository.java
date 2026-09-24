package com.riceleaf.repository;

import com.riceleaf.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByRecordIdOrderBySortOrderAsc(Long recordId);
    Image findFirstByRecordIdOrderBySortOrderAsc(Long recordId);
}
