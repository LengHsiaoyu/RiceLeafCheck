"""Rice leaf disease inference server.

Provides a Flask HTTP API for disease detection from leaf images.

Endpoints:
    POST /detect  — multipart image upload, returns disease classification
    GET  /health  — health check

Usage:
    python inference_server.py                    # default port 5000
    RICELEAF_PORT=8082 python inference_server.py  # custom port
    gunicorn -w 2 -b 0.0.0.0:5000 inference_server:app  # production
"""

import io
import os
import traceback

import cv2
import numpy as np
import torch
import torch.nn.functional as F
from flask import Flask, jsonify, request
from PIL import Image
from torchvision import transforms

import config
from config import DISEASE_CLASSES, LEVEL_THRESHOLDS
from model import load_model

app = Flask(__name__)

_transform = transforms.Compose([
    transforms.Resize((config.IMAGE_SIZE, config.IMAGE_SIZE)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406],
                         std=[0.229, 0.224, 0.225]),
])

_device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
_model = None

SYMPTOM_DESC = {
    "健康": "叶片健康，无明显病斑",
    "叶烫病": "叶尖或叶缘呈水渍状褪绿，后变黄白色干枯，病健交界明显，潮湿时有灰色霉层",
    "白叶枯病": "沿叶缘或叶脉呈黄白色长条状，病部与健部界限呈波浪纹，湿度大时表面有淡黄色菌脓",
    "稻瘟病": "梭形或圆形斑，边缘褐色，中央灰白色，严重时病斑连片，潮湿时背面有灰色霉层",
    "窄褐斑病": "叶片上出现细窄褐色条斑，宽1-2mm，长可达数厘米，边缘清晰，严重时密集成片",
    "纹枯病": "叶鞘及叶片上云纹状大斑，边缘深褐色，病部可见白色菌丝团或褐色菌核",
    "胡麻叶斑病": "芝麻粒大小褐色至红褐色斑点，边缘黄色晕圈，严重时连成不规则大斑",
}


def get_model():
    global _model
    if _model is None:
        if os.path.exists(config.OUTPUT_MODEL_PATH):
            _model = load_model(config.OUTPUT_MODEL_PATH, config.NUM_CLASSES)
            print(f"Model loaded from {config.OUTPUT_MODEL_PATH}")
        else:
            print(f"WARNING: Model not found at {config.OUTPUT_MODEL_PATH}")
            print("Using untrained model — predictions will be random.")
            from model import create_model
            _model = create_model(config.NUM_CLASSES, pretrained=False)
        _model.to(_device)
        _model.eval()
    return _model


def estimate_lesion_ratio(image_bgr: np.ndarray) -> float:
    """Estimate lesion area ratio using HSV color thresholding.

    Healthy leaf tissue is green. Lesions appear as brown/yellow/white areas.
    Returns the ratio of non-green area to total leaf area (as percentage).
    """
    if image_bgr is None or image_bgr.size == 0:
        return 0.0

    hsv = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2HSV)

    lower_green = np.array([25, 40, 40])
    upper_green = np.array([90, 255, 255])
    green_mask = cv2.inRange(hsv, lower_green, upper_green)

    gray = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2GRAY)
    _, leaf_mask = cv2.threshold(gray, 30, 255, cv2.THRESH_BINARY)

    leaf_pixels = cv2.countNonZero(leaf_mask)
    green_pixels = cv2.countNonZero(cv2.bitwise_and(green_mask, leaf_mask))

    if leaf_pixels == 0:
        return 0.0

    lesion_pixels = leaf_pixels - green_pixels
    ratio = (lesion_pixels / leaf_pixels) * 100.0
    return round(max(0.0, min(100.0, ratio)), 2)


def lesion_ratio_to_severity(ratio: float) -> int:
    """Map lesion area ratio to severity level (0/1/3/5/7/9)."""
    for level, threshold in LEVEL_THRESHOLDS:
        if ratio <= threshold:
            return level
    return 9


@app.route("/detect", methods=["POST"])
def detect():
    if "image" not in request.files:
        return jsonify({"error": "No image provided"}), 400

    file = request.files["image"]
    if file.filename == "":
        return jsonify({"error": "Empty filename"}), 400

    try:
        image_bytes = file.read()
        pil_image = Image.open(io.BytesIO(image_bytes)).convert("RGB")

        nparr = np.frombuffer(image_bytes, np.uint8)
        image_bgr = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        model = get_model()
        input_tensor = _transform(pil_image).unsqueeze(0).to(_device)

        with torch.no_grad():
            logits = model(input_tensor)
            probs = F.softmax(logits, dim=1)
            confidence, predicted = probs.max(1)
            confidence = confidence.item()
            class_idx = predicted.item()

        disease_name = DISEASE_CLASSES[class_idx]

        if disease_name == "健康":
            lesion_ratio = 0.0
            severity = 0
        else:
            lesion_ratio = estimate_lesion_ratio(image_bgr)
            severity = lesion_ratio_to_severity(lesion_ratio)

        top3_probs, top3_indices = probs.topk(min(3, len(DISEASE_CLASSES)))
        top3 = [
            {"disease": DISEASE_CLASSES[idx.item()],
             "confidence": round(prob.item(), 4)}
            for prob, idx in zip(top3_probs.squeeze(0), top3_indices.squeeze(0))
        ]

        symptom = SYMPTOM_DESC.get(disease_name, "")

        return jsonify({
            "diseaseName": disease_name,
            "confidence": round(confidence, 4),
            "lesionAreaRatio": lesion_ratio,
            "severityLevel": severity,
            "symptomDesc": symptom,
            "top3Predictions": top3,
        })

    except Exception as e:
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500


@app.route("/health", methods=["GET"])
def health():
    model_loaded = _model is not None or os.path.exists(config.OUTPUT_MODEL_PATH)
    return jsonify({
        "status": "ok",
        "model_loaded": model_loaded,
        "device": str(_device),
        "classes": DISEASE_CLASSES,
    })


if __name__ == "__main__":
    print(f"Starting rice leaf detection server on {config.SERVER_HOST}:{config.SERVER_PORT}")
    print(f"Model path: {config.OUTPUT_MODEL_PATH}")
    print(f"Device: {_device}")
    get_model()
    app.run(host=config.SERVER_HOST, port=config.SERVER_PORT, debug=False)
