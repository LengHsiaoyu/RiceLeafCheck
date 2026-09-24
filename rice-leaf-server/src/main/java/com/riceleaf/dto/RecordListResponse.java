package com.riceleaf.dto;

import java.time.LocalDateTime;
import java.util.List;

public class RecordListResponse {
    private long total;
    private int page;
    private int size;
    private List<RecordItem> records;

    public static class RecordItem {
        private Long id;
        private String samplePoint;
        private String plantNo;
        private String leafPosition;
        private String diseaseName;
        private Integer severityLevel;
        private String thumbnailUrl;
        private LocalDateTime createTime;

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
        public Integer getSeverityLevel() { return severityLevel; }
        public void setSeverityLevel(Integer severityLevel) { this.severityLevel = severityLevel; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public List<RecordItem> getRecords() { return records; }
    public void setRecords(List<RecordItem> records) { this.records = records; }
}
