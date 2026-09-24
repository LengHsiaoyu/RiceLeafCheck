package com.riceleaf.controller;

import com.riceleaf.dto.ApiResponse;
import com.riceleaf.entity.DiseaseDict;
import com.riceleaf.service.DiseaseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/diseases")
public class DiseaseController {

    private final DiseaseService diseaseService;

    public DiseaseController(DiseaseService diseaseService) {
        this.diseaseService = diseaseService;
    }

    @GetMapping("/list")
    public ApiResponse<List<DiseaseDict>> list() {
        return ApiResponse.ok(diseaseService.getAll());
    }
}
