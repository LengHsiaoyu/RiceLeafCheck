package com.riceleaf.controller;

import com.riceleaf.dto.*;
import com.riceleaf.service.RecordService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/records")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping
    public ApiResponse<RecordListResponse> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long diseaseId) {
        return ApiResponse.ok(recordService.list(page, size, startDate, endDate, diseaseId));
    }

    @GetMapping("/{id}")
    public ApiResponse<RecordDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(recordService.detail(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody RecordUpdateRequest req) {
        recordService.update(id, req);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        recordService.delete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/count")
    public ApiResponse<Long> count() {
        return ApiResponse.ok(recordService.count());
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(defaultValue = "xlsx") String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        byte[] data = recordService.export(format, startDate, endDate);
        HttpHeaders headers = new HttpHeaders();
        String filename = "rice-leaf-records-" + LocalDate.now() + "." + format;
        headers.setContentDispositionFormData("attachment", filename);
        if ("xlsx".equals(format)) {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        } else {
            headers.setContentType(MediaType.TEXT_PLAIN);
        }
        return ResponseEntity.ok().headers(headers).body(data);
    }
}
