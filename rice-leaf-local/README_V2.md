# RiceLeafLocal V2 — 水稻叶片病害本地检测

完全离线的 Android 应用，使用 ONNX Runtime 在手机端完成叶片病害识别，无需网络连接。

## 环境要求

| 工具 | 版本 |
|------|------|
| Android Studio | Hedgehog (2023.1) 以上 |
| JDK | 17 |
| Gradle | 8.5 (wrapper 自带) |
| Android SDK | API 34 |
| Kotlin | 1.9.22 |
| minSdk / targetSdk | 26 / 34 |

## 快速开始

### 1. 进入项目目录

```bash
cd /home/lxy/code/RiceLeafCheck/rice-leaf-local
```

### 2. 配置 local.properties

```bash
echo "sdk.dir=/home/lxy/Android" > local.properties
```

将其中的路径替换为你本机的 Android SDK 路径。

### 3. 导出 ONNX 模型

如果 `app/src/main/assets/models/rice_leaf_model.onnx` 已存在则跳过此步。否则需要从训练好的 `.pth` 模型导出：

```bash
cd ../rice-leaf-ml
pip install --break-system-packages onnx onnxscript torch torchvision
python3 export_onnx.py
cp checkpoints/rice_leaf_model.onnx ../rice-leaf-local/app/src/main/assets/models/
```

### 4. 编译安装

```bash
# 编译 debug APK
./gradlew :app:assembleDebug

# 安装到已连接的设备（USB 调试需开启）
./gradlew :app:installDebug

# 或者手动安装
adb install app/build/outputs/apk/debug/app-debug.apk
```

编译成功后 APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。

### 5. 在 Android Studio 中打开

1. File → Open → 选择 `rice-leaf-local` 目录
2. 等待 Gradle Sync 完成
3. 点击 Run 按钮运行到设备

## 使用说明

### 拍照识别

1. 打开应用，进入「拍照」页面
2. 将水稻叶片平放在深色背景上，确保光照均匀
3. 输入**株号**，选择**叶片位**（可选自定义添加）
4. 点击中间 **⭕** 按钮拍照
5. 预览照片：
   - 点击 **✓** 确认，应用自动进行推理识别
   - 点击 **✕** 丢弃，重新拍照（不保存任何数据）
6. 识别结果以顶部弹窗展示，包含病害名称、置信度、病斑面积、病情级别
7. 点击「关闭」继续下一张

### 查看数据

- 底部切换到「数据」页，查看所有历史记录
- 点击记录查看详情、修改备注、删除
- 点击顶部导出按钮生成 Excel 文件

### 设置

- 「设置」页包含病害对照表、病情级别说明、使用说明

## 项目结构

```
app/src/main/java/com/riceleaf/local/
├── RiceLeafLocalApp.kt          # @HiltAndroidApp
├── MainActivity.kt              # @AndroidEntryPoint
├── di/AppModule.kt              # Hilt 依赖注入
├── ml/
│   ├── InferenceEngine.kt       # ONNX Runtime 推理
│   ├── InferenceResult.kt       # 结果数据类
│   ├── ModelInfo.kt             # 模型配置
│   ├── Preprocessor.kt          # 图像预处理
│   ├── LesionEstimator.kt       # 病斑面积估算
│   └── SeverityMapper.kt        # 病情级别映射
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt       # Room 数据库
│   │   ├── dao/RecordDao.kt
│   │   └── entity/RecordEntity.kt
│   └── repository/PhotoRepository.kt
├── ui/
│   ├── camera/
│   │   ├── CameraScreen.kt      # 拍照界面（三段按钮）
│   │   └── CameraViewModel.kt   # 状态机
│   ├── data/
│   │   ├── DataListScreen.kt    # 记录列表
│   │   └── DataViewModel.kt
│   ├── navigation/AppNavigation.kt
│   ├── settings/SettingsScreen.kt
│   └── theme/Theme.kt
└── util/
    ├── SettingsManager.kt
    ├── FileUtil.kt
    └── ExcelExportUtil.kt
```

## 相机状态机

```
IDLE ──(拍照)──> CAPTURED ──(✓)──> INFERRING ──> RESULT ──> IDLE
                    │                      │
                    └──(✕)──> IDLE         └──(异常)──> ERROR ──> IDLE
```

## 可检测病害（7 类）

| 病害 | 典型症状 |
|------|---------|
| 健康 | 叶片无病斑，颜色正常 |
| 叶烫病 | 水渍状斑点，褐色不规则斑块 |
| 白叶枯病 | 叶尖黄白色条纹，灰白色枯斑 |
| 稻瘟病 | 梭形褐色病斑，中央灰白 |
| 穗颈瘟 | 穗颈和枝梗褐色坏死，导致白穗秕粒 |
| 窄褐斑病 | 窄长形褐色斑点，沿叶脉分布 |
| 纹枯病 | 云纹状褐色斑块，湿度大见菌丝 |
| 胡麻叶斑病 | 圆形褐色小斑点，形似胡麻 |

## 病情分级

| 级别 | 病斑面积占比 |
|------|------------|
| 0 | 0%（健康） |
| 1 | 0.1% ~ 5.0% |
| 3 | 5.1% ~ 10.0% |
| 5 | 10.1% ~ 25.0% |
| 7 | 25.1% ~ 50.0% |
| 9 | 50.1% ~ 100% |

## 技术栈

- **推理引擎**: ONNX Runtime Mobile 1.18.0
- **模型架构**: ResNet50（PyTorch → ONNX 导出）
- **UI**: Jetpack Compose + Material3
- **相机**: CameraX 1.3.1
- **数据库**: Room 2.6.1
- **DI**: Hilt 2.50
- **图片加载**: Coil 2.5.0
- **Excel 导出**: Apache POI 5.2.5
