# """Rice leaf disease classification — training script.

# Usage:
#     python train.py                             # train with default settings
#     python train.py --epochs 80 --lr 5e-5       # custom hyperparams
#     python train.py --data_dir /path/to/dataset  # custom data path

# Expected dataset structure:
#     dataset/
#     ├── train/
#     │   ├── 健康/
#     │   ├── 叶烫病/
#     │   ├── 白叶枯病/
#     │   ├── 稻瘟病/
#     │   ├── 穗颈瘟/
#     │   ├── 窄褐斑病/
#     │   ├── 纹枯病/
#     │   └── 胡麻叶斑病/
#     └── val/
#         └── (same subdirs)
# """

# import argparse
# import os

# import torch
# import torch.nn as nn
# from torch.optim import AdamW
# from torch.optim.lr_scheduler import CosineAnnealingLR
# from torch.utils.data import DataLoader
# from sklearn.metrics import classification_report

# import config
# from config import DISEASE_CLASSES, NUM_CLASSES
# from dataset import RiceLeafDataset, get_train_transform, get_val_transform
# from model import create_model


# def train_one_epoch(model, loader, criterion, optimizer, device, epoch):
#     model.train()
#     running_loss = 0.0
#     correct = 0
#     total = 0

#     for batch_idx, (images, labels) in enumerate(loader):
#         images, labels = images.to(device), labels.to(device)

#         optimizer.zero_grad()
#         outputs = model(images)
#         loss = criterion(outputs, labels)
#         loss.backward()
#         optimizer.step()

#         running_loss += loss.item()
#         _, predicted = outputs.max(1)
#         total += labels.size(0)
#         correct += predicted.eq(labels).sum().item()

#     avg_loss = running_loss / len(loader)
#     accuracy = 100.0 * correct / total
#     return avg_loss, accuracy


# @torch.no_grad()
# def validate(model, loader, criterion, device, epoch):
#     model.eval()
#     running_loss = 0.0
#     correct = 0
#     total = 0
#     all_preds = []
#     all_labels = []

#     for images, labels in loader:
#         images, labels = images.to(device), labels.to(device)
#         outputs = model(images)
#         loss = criterion(outputs, labels)

#         running_loss += loss.item()
#         _, predicted = outputs.max(1)
#         total += labels.size(0)
#         correct += predicted.eq(labels).sum().item()
#         all_preds.extend(predicted.cpu().tolist())
#         all_labels.extend(labels.cpu().tolist())

#     avg_loss = running_loss / len(loader)
#     accuracy = 100.0 * correct / total

#     if epoch % 5 == 0:
#         report = classification_report(all_labels, all_preds,
#                                        target_names=DISEASE_CLASSES,
#                                        zero_division=0)
#         print(f"\nClassification Report (epoch {epoch}):\n{report}")

#     return avg_loss, accuracy


# def main():
#     parser = argparse.ArgumentParser()
#     parser.add_argument("--epochs", type=int, default=config.NUM_EPOCHS)
#     parser.add_argument("--batch_size", type=int, default=config.BATCH_SIZE)
#     parser.add_argument("--lr", type=float, default=config.LEARNING_RATE)
#     parser.add_argument("--data_dir", type=str, default=config.DATA_DIR)
#     parser.add_argument("--image_size", type=int, default=config.IMAGE_SIZE)
#     args = parser.parse_args()

#     device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
#     print(f"Using device: {device}")
#     print(f"Data directory: {args.data_dir}")

#     os.makedirs(config.MODEL_DIR, exist_ok=True)

#     train_dir = os.path.join(args.data_dir, "train")
#     val_dir = os.path.join(args.data_dir, "val")

#     if not os.path.isdir(train_dir):
#         print(f"ERROR: Train directory not found: {train_dir}")
#         print("Expected structure: dataset/train/{class_name}/*.jpg")
#         return

#     train_dataset = RiceLeafDataset(train_dir, DISEASE_CLASSES,
#                                     transform=get_train_transform(args.image_size))
#     val_dataset = RiceLeafDataset(val_dir, DISEASE_CLASSES,
#                                   transform=get_val_transform(args.image_size))

#     print(f"Train samples: {len(train_dataset)}, Val samples: {len(val_dataset)}")

#     class_weights = train_dataset.get_class_weights().to(device)
#     print(f"Class weights: {class_weights.tolist()}")

#     train_loader = DataLoader(train_dataset, batch_size=args.batch_size,
#                               shuffle=True, num_workers=config.NUM_WORKERS)
#     val_loader = DataLoader(val_dataset, batch_size=args.batch_size,
#                             shuffle=False, num_workers=config.NUM_WORKERS) if len(val_dataset) > 0 else None

#     model = create_model(NUM_CLASSES, pretrained=True)
#     model.to(device)

#     criterion = nn.CrossEntropyLoss(weight=class_weights)
#     optimizer = AdamW(model.fc.parameters(), lr=args.lr)
#     scheduler = CosineAnnealingLR(optimizer, T_max=args.epochs)

#     # Phase 1: train classification head only
#     print("\n=== Phase 1: Training classification head ===")
#     best_acc = 0.0
#     for epoch in range(args.epochs // 2):
#         train_loss, train_acc = train_one_epoch(model, train_loader, criterion,
#                                                  optimizer, device, epoch)
#         print(f"Epoch {epoch:3d} | Train Loss: {train_loss:.4f} | Train Acc: {train_acc:.2f}%")

#         if val_loader:
#             val_loss, val_acc = validate(model, val_loader, criterion, device, epoch)
#             print(f"         | Val Loss:   {val_loss:.4f} | Val Acc:   {val_acc:.2f}%")
#         else:
#             val_acc = train_acc

#         scheduler.step()

#         if val_acc > best_acc:
#             best_acc = val_acc
#             torch.save(model.state_dict(),
#                        os.path.join(config.MODEL_DIR, "best_phase1.pth"))

#     # Phase 2: fine-tune entire model
#     print("\n=== Phase 2: Fine-tuning entire model ===")
#     for param in model.parameters():
#         param.requires_grad = True

#     optimizer = AdamW(model.parameters(), lr=args.lr * 0.1)
#     scheduler = CosineAnnealingLR(optimizer, T_max=args.epochs - args.epochs // 2)

#     for epoch in range(args.epochs // 2, args.epochs):
#         train_loss, train_acc = train_one_epoch(model, train_loader, criterion,
#                                                  optimizer, device, epoch)
#         print(f"Epoch {epoch:3d} | Train Loss: {train_loss:.4f} | Train Acc: {train_acc:.2f}%")

#         if val_loader:
#             val_loss, val_acc = validate(model, val_loader, criterion, device, epoch)
#             print(f"         | Val Loss:   {val_loss:.4f} | Val Acc:   {val_acc:.2f}%")
#         else:
#             val_acc = train_acc

#         scheduler.step()

#         if val_acc > best_acc:
#             best_acc = val_acc
#             torch.save(model.state_dict(), config.OUTPUT_MODEL_PATH)

#     torch.save(model.state_dict(), config.OUTPUT_MODEL_PATH)
#     print(f"\nTraining complete. Best val accuracy: {best_acc:.2f}%")
#     print(f"Model saved to: {config.OUTPUT_MODEL_PATH}")

#     print("\nClass distribution in training set:")
#     for i, name in enumerate(DISEASE_CLASSES):
#         count = sum(1 for _, l in train_dataset.samples if l == i)
#         print(f"  {name}: {count} samples")


# if __name__ == "__main__":
#     main()
