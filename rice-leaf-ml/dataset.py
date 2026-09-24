import os

import torch
from PIL import Image
from torch.utils.data import Dataset
from torchvision import transforms


def get_train_transform(image_size: int):
    return transforms.Compose([
        transforms.RandomResizedCrop(image_size, scale=(0.85, 1.0)),
        transforms.RandomHorizontalFlip(),
        transforms.RandomVerticalFlip(),
        transforms.RandomRotation(10),
        transforms.ColorJitter(brightness=0.1, contrast=0.1, saturation=0.1, hue=0.05),
        transforms.ToTensor(),
        transforms.Normalize(mean=[0.485, 0.456, 0.406],
                             std=[0.229, 0.224, 0.225]),
    ])


def get_val_transform(image_size: int):
    return transforms.Compose([
        transforms.Resize((image_size, image_size)),
        transforms.ToTensor(),
        transforms.Normalize(mean=[0.485, 0.456, 0.406],
                             std=[0.229, 0.224, 0.225]),
    ])


class RiceLeafDataset(Dataset):
    """Dataset for rice leaf disease images.

    Expected directory structure:
        dataset/
        ├── train/
        │   ├── 稻瘟病（叶瘟）/
        │   ├── 纹枯病/
        │   ├── ...
        │   └── 健康/
        └── val/
            └── ...
    """

    def __init__(self, root_dir: str, class_names: list[str], transform=None):
        self.root_dir = root_dir
        self.class_names = class_names
        self.class_to_idx = {name: i for i, name in enumerate(class_names)}
        self.transform = transform
        self.samples: list[tuple[str, int]] = []

        for class_name in class_names:
            class_dir = os.path.join(root_dir, class_name)
            if not os.path.isdir(class_dir):
                continue
            for fname in os.listdir(class_dir):
                if fname.lower().endswith((".jpg", ".jpeg", ".png", ".bmp")):
                    self.samples.append((os.path.join(class_dir, fname),
                                         self.class_to_idx[class_name]))

    def __len__(self):
        return len(self.samples)

    def __getitem__(self, idx):
        path, label = self.samples[idx]
        image = Image.open(path).convert("RGB")
        if self.transform:
            image = self.transform(image)
        return image, label

    def get_class_weights(self) -> torch.Tensor:
        """Compute inverse-frequency class weights for imbalanced datasets."""
        counts = torch.zeros(len(self.class_names))
        for _, label in self.samples:
            counts[label] += 1
        weights = 1.0 / (counts + 1)
        return weights / weights.sum() * len(self.class_names)
