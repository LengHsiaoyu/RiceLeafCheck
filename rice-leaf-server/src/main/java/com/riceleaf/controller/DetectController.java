package com.riceleaf.controller;

import com.riceleaf.dto.ApiResponse;
import com.riceleaf.dto.DetectResponse;
import com.riceleaf.service.RecordService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/diseases")
public class DetectController {

    private final RecordService recordService;

    public DetectController(RecordService recordService) {
        this.recordService = recordService;
    }

    @PostMapping(value = "/detect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<DetectResponse> detect(
            @RequestParam("image") MultipartFile image,
            @RequestParam("samplePoint") String samplePoint,
            @RequestParam("plantNo") String plantNo,
            @RequestParam("leafPosition") String leafPosition,
            @RequestParam(defaultValue = "cloud") String model) throws IOException {
        return ApiResponse.ok(recordService.detect(image, samplePoint, plantNo, leafPosition, model));
    }
}
