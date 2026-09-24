package com.riceleaf.service;

import com.riceleaf.dto.*;
import com.riceleaf.entity.Image;
import com.riceleaf.entity.Record;
import com.riceleaf.entity.DiseaseDict;
import com.riceleaf.repository.*;
import com.riceleaf.service.strategy.DetectResult;
import com.riceleaf.service.strategy.DiseaseDetectStrategy;
import com.riceleaf.util.ExcelUtil;
import com.riceleaf.util.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RecordService {

    private static final Logger log = LoggerFactory.getLogger(RecordService.class);

    private final RecordRepository recordRepo;
    private final ImageRepository imageRepo;
    private final DiseaseDictRepository diseaseRepo;
    private final DiseaseDetectStrategy mockStrategy;
    private final DiseaseDetectStrategy cloudStrategy;
    private final String baseDir;

    public RecordService(RecordRepository recordRepo,
                         ImageRepository imageRepo,
                         DiseaseDictRepository diseaseRepo,
                         @Qualifier("mock") DiseaseDetectStrategy mockStrategy,
                         @Qualifier("cloud") DiseaseDetectStrategy cloudStrategy,
                         @Value("${app.image.base-dir}") String baseDir) {
        this.recordRepo = recordRepo;
        this.imageRepo = imageRepo;
        this.diseaseRepo = diseaseRepo;
        this.mockStrategy = mockStrategy;
        this.cloudStrategy = cloudStrategy;
        this.baseDir = baseDir;
    }

    @Transactional
    public DetectResponse detect(MultipartFile file, String samplePoint,
                                  String plantNo, String leafPosition, String model) throws IOException {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String uuid = UUID.randomUUID().toString();
        String ext = getExtension(file.getOriginalFilename());
        String imagePath = dateStr + "/" + uuid + "." + ext;

        BufferedImage original = ImageIO.read(file.getInputStream());
        ImageUtil.saveImage(original, baseDir + "/" + imagePath);

        String thumbName = uuid + "_thumb.jpg";
        String thumbDir = baseDir + "/thumb/" + dateStr;
        ImageUtil.saveThumbnail(original, thumbDir, thumbName, 300, 400);
        String thumbPath = "thumb/" + dateStr + "/" + thumbName;

        DiseaseDetectStrategy strategy = "mock".equals(model) ? mockStrategy : cloudStrategy;
        DetectResult result = strategy.detect(original);
        if (result == null && strategy == cloudStrategy) {
            log.warn("Cloud detection returned null, falling back to mock");
            result = mockStrategy.detect(original);
        }

        Record record = new Record();
        record.setSamplePoint(samplePoint);
        record.setPlantNo(plantNo);
        record.setLeafPosition(leafPosition);
        if (result != null) {
            record.setDiseaseId(result.getDiseaseId());
            record.setConfidence(result.getConfidence());
            record.setLesionRatio(result.getLesionAreaRatio());
            record.setSeverityLevel(result.getSeverityLevel());
        }
        record.setStatus(0);
        recordRepo.save(record);

        Image img = new Image();
        img.setRecordId(record.getId());
        img.setImagePath(imagePath);
        img.setThumbPath(thumbPath);
        img.setFileSize(file.getSize());
        img.setSortOrder(0);
        imageRepo.save(img);

        DetectResponse resp = new DetectResponse();
        resp.setRecordId(record.getId());
        resp.setImageUrl("/images/" + imagePath);
        resp.setThumbnailUrl("/images/" + thumbPath);
        resp.setCreateTime(record.getCreatedAt());
        if (result != null) {
            resp.setDiseaseName(result.getDiseaseName());
            resp.setConfidence(result.getConfidence());
            resp.setLesionAreaRatio(result.getLesionAreaRatio());
            resp.setSeverityLevel(result.getSeverityLevel());
            resp.setSymptomDesc(result.getSymptomDesc());
        }
        return resp;
    }

    public long count() {
        return recordRepo.countByStatusNot(1);
    }

    public RecordListResponse list(int page, int size, LocalDateTime startDate,
                                    LocalDateTime endDate, Long diseaseId) {
        Page<Record> recordPage = recordRepo.findFiltered(startDate, endDate, diseaseId,
                PageRequest.of(page - 1, size));

        List<RecordListResponse.RecordItem> items = recordPage.getContent().stream().map(r -> {
            RecordListResponse.RecordItem item = new RecordListResponse.RecordItem();
            item.setId(r.getId());
            item.setSamplePoint(r.getSamplePoint());
            item.setPlantNo(r.getPlantNo());
            item.setLeafPosition(r.getLeafPosition());
            item.setSeverityLevel(r.getSeverityLevel());
            item.setCreateTime(r.getCreatedAt());

            if (r.getDiseaseId() != null) {
                diseaseRepo.findById(r.getDiseaseId())
                        .ifPresent(d -> item.setDiseaseName(d.getName()));
            }

            Image img = imageRepo.findFirstByRecordIdOrderBySortOrderAsc(r.getId());
            if (img != null) {
                item.setThumbnailUrl("/images/" + img.getThumbPath());
            }
            return item;
        }).collect(Collectors.toList());

        RecordListResponse resp = new RecordListResponse();
        resp.setTotal(recordPage.getTotalElements());
        resp.setPage(page);
        resp.setSize(size);
        resp.setRecords(items);
        return resp;
    }

    public RecordDetailResponse detail(Long id) {
        Record r = recordRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));

        RecordDetailResponse resp = new RecordDetailResponse();
        resp.setId(r.getId());
        resp.setSamplePoint(r.getSamplePoint());
        resp.setPlantNo(r.getPlantNo());
        resp.setLeafPosition(r.getLeafPosition());
        resp.setConfidence(r.getConfidence());
        resp.setLesionAreaRatio(r.getLesionRatio());
        resp.setSeverityLevel(r.getSeverityLevel());
        resp.setRemark(r.getRemark());
        resp.setCreateTime(r.getCreatedAt());
        resp.setUpdateTime(r.getUpdatedAt());

        if (r.getDiseaseId() != null) {
            diseaseRepo.findById(r.getDiseaseId()).ifPresent(d -> {
                resp.setDiseaseName(d.getName());
                resp.setSymptomDesc(d.getSymptom());
            });
        }

        Image img = imageRepo.findFirstByRecordIdOrderBySortOrderAsc(r.getId());
        if (img != null) {
            resp.setImageUrl("/images/" + img.getImagePath());
        }
        return resp;
    }

    @Transactional
    public void update(Long id, RecordUpdateRequest req) {
        Record r = recordRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        if (req.getSamplePoint() != null) r.setSamplePoint(req.getSamplePoint());
        if (req.getPlantNo() != null) r.setPlantNo(req.getPlantNo());
        if (req.getLeafPosition() != null) r.setLeafPosition(req.getLeafPosition());
        if (req.getDiseaseId() != null) r.setDiseaseId(req.getDiseaseId());
        if (req.getLesionAreaRatio() != null) r.setLesionRatio(req.getLesionAreaRatio());
        if (req.getSeverityLevel() != null) r.setSeverityLevel(req.getSeverityLevel());
        if (req.getRemark() != null) r.setRemark(req.getRemark());
        recordRepo.save(r);
    }

    @Transactional
    public void delete(Long id) {
        Record r = recordRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        r.setStatus(1);
        recordRepo.save(r);
    }

    public byte[] export(String format, LocalDateTime startDate, LocalDateTime endDate) {
        List<Record> records = recordRepo.findAllForExport(startDate, endDate);
        return ExcelUtil.generateExcel(records, diseaseRepo);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
