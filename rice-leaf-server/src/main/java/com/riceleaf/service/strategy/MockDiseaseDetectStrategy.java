package com.riceleaf.service.strategy;

import com.riceleaf.entity.DiseaseDict;
import com.riceleaf.repository.DiseaseDictRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

@Component
@Qualifier("mock")
public class MockDiseaseDetectStrategy implements DiseaseDetectStrategy {

    private final DiseaseDictRepository diseaseRepo;

    private static final int[] LEVELS = {0, 1, 3, 5, 7, 9};
    private static final double[][] RANGES = {
        {0, 0}, {0.1, 5.0}, {6.0, 10.0}, {11.0, 25.0}, {26.0, 50.0}, {51.0, 100.0}
    };

    public MockDiseaseDetectStrategy(DiseaseDictRepository diseaseRepo) {
        this.diseaseRepo = diseaseRepo;
    }

    @Override
    public DetectResult detect(BufferedImage image) {
        List<DiseaseDict> diseases = diseaseRepo.findAllByOrderBySortOrderAsc();
        if (diseases.isEmpty()) {
            return null;
        }

        long seed = computeImageHash(image);
        Random random = new Random(seed);

        DiseaseDict disease = diseases.get(random.nextInt(diseases.size()));
        int idx = random.nextInt(1, LEVELS.length);
        int level = LEVELS[idx];
        double min = RANGES[idx][0];
        double max = RANGES[idx][1];
        double ratio = min + random.nextDouble() * (max - min);
        double conf = 0.70 + random.nextDouble() * 0.29;

        BigDecimal confidence = BigDecimal.valueOf(conf).setScale(4, RoundingMode.HALF_UP);
        BigDecimal lesionRatio = BigDecimal.valueOf(ratio).setScale(2, RoundingMode.HALF_UP);

        return new DetectResult(disease.getId(), disease.getName(),
                confidence, lesionRatio, level, disease.getSymptom());
    }

    private long computeImageHash(BufferedImage image) {
        long hash = 0;
        int w = image.getWidth();
        int h = image.getHeight();
        int step = Math.max(1, Math.min(w, h) / 20);
        for (int y = 0; y < h; y += step) {
            for (int x = 0; x < w; x += step) {
                hash = hash * 31 + image.getRGB(x, y);
            }
        }
        return hash;
    }
}
