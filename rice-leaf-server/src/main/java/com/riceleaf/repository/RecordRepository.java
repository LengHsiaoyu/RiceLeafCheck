package com.riceleaf.repository;

import com.riceleaf.entity.Record;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface RecordRepository extends JpaRepository<Record, Long> {

    long countByStatusNot(Integer status);

    @Query("SELECT r FROM Record r WHERE r.status = 0 " +
           "AND (:startDate IS NULL OR r.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR r.createdAt <= :endDate) " +
           "AND (:diseaseId IS NULL OR r.diseaseId = :diseaseId) " +
           "ORDER BY r.createdAt DESC")
    Page<Record> findFiltered(@Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate,
                              @Param("diseaseId") Long diseaseId,
                              Pageable pageable);

    @Query("SELECT r FROM Record r WHERE r.status = 0 " +
           "AND (:startDate IS NULL OR r.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR r.createdAt <= :endDate) " +
           "ORDER BY r.createdAt DESC")
    List<Record> findAllForExport(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);
}
