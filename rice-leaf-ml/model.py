import torch
import torch.nn as nn
from torchvision import models


def create_model(num_classes: int, pretrained: bool = True) -> nn.Module:
    """Create a ResNet50 model for rice leaf disease classification."""
    model = models.resnet50(weights="IMAGENET1K_V2" if pretrained else None)

    for param in model.parameters():
        param.requires_grad = False

    in_features = model.fc.in_features
    model.fc = nn.Sequential(
        nn.Dropout(0.3),
        nn.Linear(in_features, 512),
        nn.ReLU(),
        nn.Dropout(0.3),
        nn.Linear(512, num_classes),
    )

    return model


def load_model(model_path: str, num_classes: int) -> nn.Module:
    """Load a trained model from checkpoint."""
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    model = create_model(num_classes, pretrained=False)
    state = torch.load(model_path, map_location=device, weights_only=True)
    model.load_state_dict(state)
    model.to(device)
    model.eval()
    return model
