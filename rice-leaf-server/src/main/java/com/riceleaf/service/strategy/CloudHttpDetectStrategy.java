package com.riceleaf.service.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.riceleaf.entity.DiseaseDict;
import com.riceleaf.repository.DiseaseDictRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Component
@Qualifier("cloud")
public class CloudHttpDetectStrategy implements DiseaseDetectStrategy {

    private static final Logger log = LoggerFactory.getLogger(CloudHttpDetectStrategy.class);
    private final DiseaseDictRepository diseaseRepo;
    private final String cloudEndpoint;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CloudHttpDetectStrategy(DiseaseDictRepository diseaseRepo,
                                    @Value("${app.detect.cloud-endpoint:}") String cloudEndpoint) {
        this.diseaseRepo = diseaseRepo;
        this.cloudEndpoint = cloudEndpoint;
    }

    @Override
    public DetectResult detect(BufferedImage image) {
        if (cloudEndpoint == null || cloudEndpoint.isBlank()) {
            log.warn("Cloud endpoint not configured");
            return null;
        }
        try {
            return callCloudApi(image);
        } catch (Exception e) {
            log.error("Cloud API call failed: {}", e.getMessage(), e);
            return null;
        }
    }

    private DetectResult callCloudApi(BufferedImage image) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, "JPEG", bos);
        byte[] imageBytes = bos.toByteArray();

        String boundary = "----RiceLeaf" + UUID.randomUUID();
        HttpURLConnection conn = (HttpURLConnection) URI.create(cloudEndpoint).toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(60000);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            os.write("Content-Disposition: form-data; name=\"image\"; filename=\"leaf.jpg\"\r\n"
                    .getBytes(StandardCharsets.UTF_8));
            os.write("Content-Type: image/jpeg\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            os.write(imageBytes);
            os.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        if (conn.getResponseCode() != 200) {
            String errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            throw new RuntimeException("Cloud API returned " + conn.getResponseCode() + ": " + errorBody);
        }

        JsonNode root = objectMapper.readTree(conn.getInputStream());
        String diseaseName = root.path("diseaseName").asText("");
        double confidence = root.path("confidence").asDouble(0.0);
        double lesionRatio = root.path("lesionAreaRatio").asDouble(0.0);
        int severityLevel = root.path("severityLevel").asInt(0);

        if (diseaseName.isEmpty()) {
            throw new RuntimeException("Cloud API returned empty disease name");
        }

        List<DiseaseDict> diseases = diseaseRepo.findAllByOrderBySortOrderAsc();
        DiseaseDict disease = diseases.stream()
                .filter(d -> d.getName().equals(diseaseName))
                .findFirst()
                .orElse(null);

        if (disease == null) {
            log.warn("Disease '{}' not found in dictionary", diseaseName);
            if (diseases.isEmpty()) return null;
            disease = diseases.get(0);
        }

        return new DetectResult(disease.getId(), disease.getName(),
                BigDecimal.valueOf(confidence).setScale(4, RoundingMode.HALF_UP),
                BigDecimal.valueOf(lesionRatio).setScale(2, RoundingMode.HALF_UP),
                severityLevel, disease.getSymptom());
    }
}
