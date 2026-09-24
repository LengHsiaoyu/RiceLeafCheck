"""Export trained .pth model to ONNX format for mobile inference."""
import torch
from model import create_model
from config import NUM_CLASSES, OUTPUT_MODEL_PATH

MODEL_PATH = OUTPUT_MODEL_PATH  # "checkpoints/rice_leaf_model.pth"
ONNX_PATH = MODEL_PATH.replace(".pth", ".onnx")

device = torch.device("cpu")
model = create_model(NUM_CLASSES, pretrained=False)
state = torch.load(MODEL_PATH, map_location=device, weights_only=True)
model.load_state_dict(state)
model.to(device)
model.eval()

dummy = torch.randn(1, 3, 224, 224, device=device)

torch.onnx.export(
    model,
    dummy,
    ONNX_PATH,
    input_names=["input"],
    output_names=["output"],
    dynamic_axes={"input": {0: "batch"}, "output": {0: "batch"}},
    opset_version=14,
)

print(f"Exported to {ONNX_PATH}")

# Verify
import onnx
onnx.checker.check_model(ONNX_PATH)
print("ONNX model verified OK")
