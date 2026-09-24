package com.riceleaf.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DetectResponse {
    private Long recordId;
    private String diseaseName;
    private BigDecimal confidence;
    private BigDecimal lesionAreaRatio;
    private Integer severityLevel;
    private String symptomDesc;
    private String imageUrl;
    private String thumbnailUrl;
    private LocalDateTime createTime;

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public String getDiseaseName() { return diseaseName; }
    public void setDiseaseName(String diseaseName) { this.diseaseName = diseaseName; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    public BigDecimal getLesionAreaRatio() { return lesionAreaRatio; }
    public void setLesionAreaRatio(BigDecimal lesionAreaRatio) { this.lesionAreaRatio = lesionAreaRatio; }
    public Integer getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(Integer severityLevel) { this.severityLevel = severityLevel; }
    public String getSymptomDesc() { return symptomDesc; }
    public void setSymptomDesc(String symptomDesc) { this.symptomDesc = symptomDesc; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
