package com.riceleaf.app.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(viewModel: CameraViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    val cameraController = remember { LifecycleCameraController(context) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    // Temporary file for capture
    var captureFile by remember { mutableStateOf<File?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission && uiState.capturedImageUri == null) {
            // Camera preview mode
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = androidx.camera.view.PreviewView(ctx)
                    cameraController.bindToLifecycle(lifecycleOwner)
                    previewView.controller = cameraController
                    previewView
                }
            )

            // Capture button overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        val file = File(context.cacheDir, "capture_${System.currentTimeMillis()}.jpg")
                        captureFile = file
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                        cameraController.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    viewModel.onPhotoCaptured(Uri.fromFile(file))
                                }
                                override fun onError(exc: ImageCaptureException) {
                                    Toast.makeText(context, "拍照失败: ${exc.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Icon(
                        Icons.Default.Camera,
                        contentDescription = "拍摄",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "拍摄",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            // Shot count badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    "已拍: ${uiState.shotCount} 张",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        } else if (!hasCameraPermission) {
            // Permission request
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("需要相机权限来拍摄叶片照片", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("授予权限")
                }
            }
        }

        // Parameter entry + preview after capture
        if (uiState.capturedImageUri != null && uiState.result == null) {
            ParameterEntryOverlay(
                imageUri = uiState.capturedImageUri!!,
                samplePoint = uiState.samplePoint,
                plantNo = uiState.plantNo,
                leafPosition = uiState.leafPosition,
                isUploading = uiState.isUploading,
                onSamplePointChange = viewModel::updateSamplePoint,
                onPlantNoChange = viewModel::updatePlantNo,
                onLeafPositionChange = viewModel::updateLeafPosition,
                onUpload = viewModel::upload,
                onRetake = viewModel::retake
            )
        }

        // Result card overlay
        if (uiState.result != null) {
            ResultCard(
                result = uiState.result!!,
                onContinue = viewModel::continueShooting,
                onRetake = viewModel::retake
            )
        }

        // Error snackbar
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(error)
            }
        }
    }
}

@Composable
fun ParameterEntryOverlay(
    imageUri: Uri,
    samplePoint: String,
    plantNo: String,
    leafPosition: String,
    isUploading: Boolean,
    onSamplePointChange: (String) -> Unit,
    onPlantNoChange: (String) -> Unit,
    onLeafPositionChange: (String) -> Unit,
    onUpload: () -> Unit,
    onRetake: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Preview image
            AsyncImage(
                model = imageUri,
                contentDescription = "拍摄的照片",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // Parameter form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("填写检测参数", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = samplePoint,
                        onValueChange = onSamplePointChange,
                        label = { Text("样点") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = plantNo,
                            onValueChange = onPlantNoChange,
                            label = { Text("株号") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        var leafExpanded by remember { mutableStateOf(false) }
                        val leafOptions = listOf("倒1叶", "倒2叶", "倒3叶")

                        ExposedDropdownMenuBox(
                            expanded = leafExpanded,
                            onExpandedChange = { leafExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = leafPosition,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("叶片位") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = leafExpanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = leafExpanded,
                                onDismissRequest = { leafExpanded = false }
                            ) {
                                leafOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            onLeafPositionChange(option)
                                            leafExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = onRetake,
                            modifier = Modifier.weight(1f)
                        ) { Text("重拍") }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = onUpload,
                            modifier = Modifier.weight(1f),
                            enabled = !isUploading
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("上传识别")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultCard(
    result: com.riceleaf.app.domain.model.DetectionResult,
    onContinue: () -> Unit,
    onRetake: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Camera,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "识别完成",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Disease info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("病害", fontSize = 12.sp, color = Color.Gray)
                        Text(result.diseaseName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("置信度", fontSize = 12.sp, color = Color.Gray)
                        Text("${(result.confidence * 100).toInt()}%",
                            fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("病斑面积占比", fontSize = 12.sp, color = Color.Gray)
                        Text("${result.lesionAreaRatio}%",
                            fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("病情级别", fontSize = 12.sp, color = Color.Gray)
                        Text("级别 ${result.severityLevel}",
                            fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (result.symptomDesc.isNotEmpty()) {
                    Text(
                        "症状: ${result.symptomDesc}",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onRetake,
                        modifier = Modifier.weight(1f)
                    ) { Text("重拍") }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = onContinue,
                        modifier = Modifier.weight(1f)
                    ) { Text("确认 & 继续拍摄") }
                }
            }
        }
    }
}
