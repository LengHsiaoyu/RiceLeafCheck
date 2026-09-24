package com.riceleaf.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RecordDetailResponse {
    private Long id;
    private String samplePoint;
    private String plantNo;
    private String leafPosition;
    private String diseaseName;
    private String symptomDesc;
    private BigDecimal confidence;
    private BigDecimal lesionAreaRatio;
    private Integer severityLevel;
    private String remark;
    private String imageUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSamplePoint() { return samplePoint; }
    public void setSamplePoint(String samplePoint) { this.samplePoint = samplePoint; }
    public String getPlantNo() { return plantNo; }
    public void setPlantNo(String plantNo) { this.plantNo = plantNo; }
    public String getLeafPosition() { return leafPosition; }
    public void setLeafPosition(String leafPosition) { this.leafPosition = leafPosition; }
    public String getDiseaseName() { return diseaseName; }
    public void setDiseaseName(String diseaseName) { this.diseaseName = diseaseName; }
    public String getSymptomDesc() { return symptomDesc; }
    public void setSymptomDesc(String symptomDesc) { this.symptomDesc = symptomDesc; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    public BigDecimal getLesionAreaRatio() { return lesionAreaRatio; }
    public void setLesionAreaRatio(BigDecimal lesionAreaRatio) { this.lesionAreaRatio = lesionAreaRatio; }
    public Integer getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(Integer severityLevel) { this.severityLevel = severityLevel; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
