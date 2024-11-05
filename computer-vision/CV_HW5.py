import os
from PIL import Image, UnidentifiedImageError
import torch
import torch.nn as nn
import torch.optim as optim
from torchvision import transforms, models
from torch.utils.data import DataLoader, random_split


class CarsCatsDataset(torch.utils.data.Dataset):
    def __init__(self, root_dir, transform=None):
        self.root_dir = root_dir
        self.transform = transform
        self.classes = ['cars', 'cats']
        self.data = []

        for i, class_name in enumerate(self.classes):
            class_path = os.path.join(self.root_dir, class_name)

            for filename in os.listdir(class_path):
                img_path = os.path.join(class_path, filename)

                try:
                    image = Image.open(img_path).convert("RGB")

                    if self.transform:
                        image = self.transform(image)

                    self.data.append((image, i))
                except (IOError, OSError, UnidentifiedImageError):
                    print(f"Skipping {img_path} - Not a valid image file!")

    def __len__(self):
        return len(self.data)

    def __getitem__(self, idx):
        image, label = self.data[idx]

        if isinstance(image, torch.Tensor):
            image = transforms.ToPILImage()(image)

        if self.transform:
            image = self.transform(image)

        return image, label


def train_model(model, train_loader, criterion, optimizer, epochs):
    for epoch in range(epochs):
        model.train()

        running_loss = 0.0

        for inputs, labels in train_loader:
            optimizer.zero_grad()

            outputs = model(inputs)
            loss = criterion(outputs, labels)

            loss.backward()
            optimizer.step()

            running_loss += loss.item()

        average_loss = running_loss / len(train_loader)

        print(f"Epoch {epoch + 1}/{epochs}, Loss: {average_loss}")


def validate_model(model, val_loader):
    model.eval()

    correct = 0
    total = 0

    with torch.no_grad():
        for inputs, labels in val_loader:
            outputs = model(inputs)
            _, predicted = torch.max(outputs.data, 1)

            total += labels.size(0)
            correct += (predicted == labels).sum().item()

            print("Predicted scores:", torch.softmax(outputs, dim=1))
            print("True labels:", labels)

    accuracy = 100 * correct / total

    print(f'Validation accuracy: {accuracy}%')


transform = transforms.Compose([
    transforms.Resize((244, 244)),
    transforms.ToTensor(),
])

dataset = CarsCatsDataset(root_dir='dataset', transform=transform)

train_size = int(0.8 * len(dataset))

print("Training size:", train_size)

val_size = len(dataset) - train_size

print("Validation size:", val_size)

train_dataset, val_dataset = random_split(dataset, [train_size, val_size])

train_loader = DataLoader(train_dataset, batch_size=50, shuffle=True)
val_loader = DataLoader(val_dataset, batch_size=50, shuffle=False)

vgg16 = models.vgg16(pretrained=True)

for param in vgg16.parameters():
    param.requires_grad = False

vgg16.classifier[6] = nn.Linear(4096, 2)

criterion = nn.CrossEntropyLoss()
optimizer = optim.SGD(vgg16.parameters(), lr=0.001, momentum=0.9)

print()
print('First training:')

train_model(vgg16, train_loader, criterion, optimizer, epochs=2)

print('First validation:')

validate_model(vgg16, val_loader)

print()
print('Second training:')

train_model(vgg16, train_loader, criterion, optimizer, epochs=5)

print('Second validation:')

validate_model(vgg16, val_loader)
