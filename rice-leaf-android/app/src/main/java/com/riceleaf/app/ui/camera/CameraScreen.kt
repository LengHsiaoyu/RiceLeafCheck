package com.riceleaf.app.ui.camera

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.YuvImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.riceleaf.app.AppSettings
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onNavigateToDetail: (Long) -> Unit,
    viewModel: CameraViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    var hasCameraPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) { permissionLauncher.launch(Manifest.permission.CAMERA) }

    val modelType by AppSettings.modelType.collectAsState()
    LaunchedEffect(modelType) { viewModel.setModelType(modelType) }

    val imageCaptureRef = remember { mutableStateOf<ImageCapture?>(null) }
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f).background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val future = ProcessCameraProvider.getInstance(ctx)
                        future.addListener({
                            val provider = future.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                                .build()
                            imageCaptureRef.value = capture
                            try {
                                provider.unbindAll()
                                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture)
                            } catch (_: Exception) {}
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("相机权限未授予", color = Color.White)
            }
        }

        // Params
        Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 4.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(value = uiState.samplePoint, onValueChange = viewModel::updateSamplePoint,
                    label = { Text("样点") }, modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(value = uiState.plantNo, onValueChange = viewModel::updatePlantNo,
                    label = { Text("株号") }, modifier = Modifier.weight(1f), singleLine = true)
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = Modifier.weight(1f)) {
                    OutlinedTextField(value = uiState.leafPosition, onValueChange = {},
                        readOnly = true, label = { Text("叶片位") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor())
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        CameraViewModel.leafPositionOptions.forEach { opt ->
                            DropdownMenuItem(text = { Text(opt) }, onClick = { viewModel.updateLeafPosition(opt); expanded = false })
                        }
                    }
                }
            }
        }

        // Bottom bar
        Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("已拍: ${uiState.shotCount} 张", style = MaterialTheme.typography.bodyMedium)
                if (uiState.isUploading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Button(
                    onClick = {
                        imageCaptureRef.value?.let { imageCapture ->
                            imageCapture.takePicture(
                                mainExecutor,
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                        val bitmap = imageProxyToBitmap(imageProxy)
                                        imageProxy.close()
                                        if (bitmap != null) {
                                            viewModel.uploadAndDetect(bitmap)
                                        } else {
                                            viewModel.onError("图片处理失败")
                                        }
                                    }
                                    override fun onError(exc: ImageCaptureException) {
                                        viewModel.onError("拍照失败: ${exc.message}")
                                    }
                                }
                            )
                        }
                    },
                    enabled = !uiState.isUploading
                ) { Text("拍摄") }
            }
        }
    }

    // Result dialog
    if (uiState.detectResult != null) {
        val r = uiState.detectResult!!
        AlertDialog(
            onDismissRequest = { viewModel.clearResult() },
            title = { Text("识别完成", fontWeight = FontWeight.Bold) },
            text = {
                @Suppress("DEPRECATION")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("病害: ${r.diseaseName}"); Text("置信度: ${(r.confidence * 100).toInt()}%")
                    Text("病斑面积: ${r.lesionAreaRatio}%"); Text("病情级别: ${r.severityLevel}")
                    r.symptomDesc?.let { Text("症状: $it", fontSize = 14.sp, color = Color.Gray) }
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.autoAdvance() }) { Text("确认 & 继续") } },
            dismissButton = { TextButton(onClick = { viewModel.clearResult() }) { Text("重拍") } }
        )
    }

    if (uiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearResult() },
            title = { Text("提示") },
            text = { Text(uiState.errorMessage!!) },
            confirmButton = { TextButton(onClick = { viewModel.clearResult() }) { Text("确定") } }
        )
    }
}

private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
    val buffer: ByteBuffer = imageProxy.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return try {
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        null
    }
}
