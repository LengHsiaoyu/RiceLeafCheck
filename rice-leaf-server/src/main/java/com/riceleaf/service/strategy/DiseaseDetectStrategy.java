package com.riceleaf.service.strategy;

import java.awt.image.BufferedImage;

public interface DiseaseDetectStrategy {
    DetectResult detect(BufferedImage image);
}
