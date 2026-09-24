import os

# Disease classes (matches disease_dict in database)
DISEASE_CLASSES = [
    "健康",
    "叶烫病",
    "白叶枯病",
    "稻瘟病",
    "穗颈瘟",
    "窄褐斑病",
    "纹枯病",
    "胡麻叶斑病",
]

NUM_CLASSES = len(DISEASE_CLASSES)

# Severity levels by lesion area ratio
LEVEL_THRESHOLDS = [
    (0, 0),        # level 0: 0%
    (1, 5.0),      # level 1: 0.1-5.0%
    (3, 10.0),     # level 3: 5.1-10.0%
    (5, 25.0),     # level 5: 10.1-25.0%
    (7, 50.0),     # level 7: 25.1-50.0%
    (9, 100.0),    # level 9: 50.1-100.0%
]

# Training config
BATCH_SIZE = 32
NUM_EPOCHS = 50
LEARNING_RATE = 1e-4
IMAGE_SIZE = 224
NUM_WORKERS = 4

# Paths
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = os.environ.get("RICELEAF_DATA_DIR", os.path.join(BASE_DIR, "dataset"))
MODEL_DIR = os.environ.get("RICELEAF_MODEL_DIR", os.path.join(BASE_DIR, "checkpoints"))
OUTPUT_MODEL_PATH = os.path.join(MODEL_DIR, "rice_leaf_model.pth")

# Inference server
SERVER_HOST = os.environ.get("RICELEAF_HOST", "0.0.0.0")
SERVER_PORT = int(os.environ.get("RICELEAF_PORT", "5000"))
