package com.riceleaf.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "record")
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sample_point", nullable = false, length = 32)
    private String samplePoint;

    @Column(name = "plant_no", nullable = false, length = 32)
    private String plantNo;

    @Column(name = "leaf_position", nullable = false, length = 16)
    private String leafPosition;

    @Column(name = "disease_id")
    private Long diseaseId;

    @Column(precision = 5, scale = 4)
    private BigDecimal confidence;

    @Column(name = "lesion_ratio", precision = 5, scale = 2)
    private BigDecimal lesionRatio;

    @Column(name = "severity_level")
    private Integer severityLevel;

    @Column(length = 256)
    private String remark;

    @Column(nullable = false)
    private Integer status = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSamplePoint() { return samplePoint; }
    public void setSamplePoint(String samplePoint) { this.samplePoint = samplePoint; }
    public String getPlantNo() { return plantNo; }
    public void setPlantNo(String plantNo) { this.plantNo = plantNo; }
    public String getLeafPosition() { return leafPosition; }
    public void setLeafPosition(String leafPosition) { this.leafPosition = leafPosition; }
    public Long getDiseaseId() { return diseaseId; }
    public void setDiseaseId(Long diseaseId) { this.diseaseId = diseaseId; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    public BigDecimal getLesionRatio() { return lesionRatio; }
    public void setLesionRatio(BigDecimal lesionRatio) { this.lesionRatio = lesionRatio; }
    public Integer getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(Integer severityLevel) { this.severityLevel = severityLevel; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
