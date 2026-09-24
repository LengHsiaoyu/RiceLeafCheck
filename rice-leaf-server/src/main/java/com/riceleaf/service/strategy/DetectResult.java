package com.riceleaf.service.strategy;

import java.math.BigDecimal;

public class DetectResult {
    private Long diseaseId;
    private String diseaseName;
    private BigDecimal confidence;
    private BigDecimal lesionAreaRatio;
    private Integer severityLevel;
    private String symptomDesc;

    public DetectResult(Long diseaseId, String diseaseName, BigDecimal confidence,
                        BigDecimal lesionAreaRatio, Integer severityLevel, String symptomDesc) {
        this.diseaseId = diseaseId;
        this.diseaseName = diseaseName;
        this.confidence = confidence;
        this.lesionAreaRatio = lesionAreaRatio;
        this.severityLevel = severityLevel;
        this.symptomDesc = symptomDesc;
    }

    public Long getDiseaseId() { return diseaseId; }
    public String getDiseaseName() { return diseaseName; }
    public BigDecimal getConfidence() { return confidence; }
    public BigDecimal getLesionAreaRatio() { return lesionAreaRatio; }
    public Integer getSeverityLevel() { return severityLevel; }
    public String getSymptomDesc() { return symptomDesc; }
}
