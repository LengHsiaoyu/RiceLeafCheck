package com.riceleaf.dto;

import java.math.BigDecimal;

public class RecordUpdateRequest {
    private String samplePoint;
    private String plantNo;
    private String leafPosition;
    private Long diseaseId;
    private BigDecimal lesionAreaRatio;
    private Integer severityLevel;
    private String remark;

    public String getSamplePoint() { return samplePoint; }
    public void setSamplePoint(String samplePoint) { this.samplePoint = samplePoint; }
    public String getPlantNo() { return plantNo; }
    public void setPlantNo(String plantNo) { this.plantNo = plantNo; }
    public String getLeafPosition() { return leafPosition; }
    public void setLeafPosition(String leafPosition) { this.leafPosition = leafPosition; }
    public Long getDiseaseId() { return diseaseId; }
    public void setDiseaseId(Long diseaseId) { this.diseaseId = diseaseId; }
    public BigDecimal getLesionAreaRatio() { return lesionAreaRatio; }
    public void setLesionAreaRatio(BigDecimal lesionAreaRatio) { this.lesionAreaRatio = lesionAreaRatio; }
    public Integer getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(Integer severityLevel) { this.severityLevel = severityLevel; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
