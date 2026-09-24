package com.riceleaf.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "disease_dict")
public class DiseaseDict {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64, unique = true)
    private String name;

    @Column(length = 256)
    private String symptom;

    @Column(name = "spot_feature", length = 256)
    private String spotFeature;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public DiseaseDict() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSymptom() { return symptom; }
    public void setSymptom(String symptom) { this.symptom = symptom; }
    public String getSpotFeature() { return spotFeature; }
    public void setSpotFeature(String spotFeature) { this.spotFeature = spotFeature; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
