package com.riceleaf.local.ui.camera

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val capturedBitmap by viewModel.capturedBitmap.collectAsState()
    var hasCameraPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) { permissionLauncher.launch(Manifest.permission.CAMERA) }

    val imageCaptureRef = remember { mutableStateOf<ImageCapture?>(null) }
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Camera preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val future = ProcessCameraProvider.getInstance(ctx)
                            future.addListener({
                                val provider = future.get()
                                val preview = androidx.camera.core.Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val capture = ImageCapture.Builder()
                                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                                    .build()
                                imageCaptureRef.value = capture
                                try {
                                    provider.unbindAll()
                                    provider.bindToLifecycle(
                                        lifecycleOwner,
                                        androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview, capture
                                    )
                                } catch (_: Exception) {}
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("相机权限未授予", color = Color.White)
                }

                // Frozen captured image overlay
                val frozen = capturedBitmap
                val state = uiState.cameraState
                if (frozen != null && state != CameraState.IDLE && state != CameraState.ERROR) {
                    Image(
                        bitmap = frozen.asImageBitmap(),
                        contentDescription = "captured",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Parameter form
            Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.plantNo,
                        onValueChange = viewModel::updatePlantNo,
                        label = { Text("株号") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = uiState.leafPosition,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("叶片位") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            uiState.leafPositionOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        viewModel.updateLeafPosition(opt)
                                        expanded = false
                                    }
                                )
                            }
                            Divider()
                            DropdownMenuItem(
                                text = { Text("+ 添加叶片位") },
                                onClick = {
                                    expanded = false
                                    showAddDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            }

            // Three-button bar
            Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ✕ Discard
                    FilledIconButton(
                        onClick = { viewModel.onDiscard() },
                        enabled = uiState.cameraState == CameraState.CAPTURED,
                        modifier = Modifier.size(56.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("✕", fontSize = 24.sp, color = Color.White)
                    }

                    // ⭕ Shoot
                    val canShoot = uiState.cameraState == CameraState.IDLE
                            || uiState.cameraState == CameraState.RESULT
                            || uiState.cameraState == CameraState.ERROR
                    FilledIconButton(
                        onClick = {
                            imageCaptureRef.value?.let { imageCapture ->
                                imageCapture.takePicture(
                                    mainExecutor,
                                    object : ImageCapture.OnImageCapturedCallback() {
                                        override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                            val bitmap = imageProxyToBitmap(imageProxy)
                                            imageProxy.close()
                                            if (bitmap != null) {
                                                viewModel.onCapture(bitmap)
                                            }
                                        }
                                        override fun onError(exc: ImageCaptureException) {
                                            // ignore
                                        }
                                    }
                                )
                            }
                        },
                        enabled = canShoot,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        if (uiState.cameraState == CameraState.INFERRING) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text("⭕", fontSize = 24.sp, color = Color.White)
                        }
                    }

                    // ✓ Confirm
                    FilledIconButton(
                        onClick = { viewModel.onConfirm() },
                        enabled = uiState.cameraState == CameraState.CAPTURED,
                        modifier = Modifier.size(56.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFF4CAF50),
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("✓", fontSize = 24.sp, color = Color.White)
                    }
                }
            }
        }

        // Result overlay — top banner
        AnimatedVisibility(
            visible = uiState.cameraState == CameraState.RESULT && uiState.inferenceResult != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            uiState.inferenceResult?.let { result ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = MaterialTheme.shapes.medium,
                    shadowElevation = 6.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("识别结果", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            TextButton(onClick = { viewModel.onDismissResult() }) {
                                Text("关闭")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("病害: ${result.className}", fontSize = 15.sp)
                        Text("置信度: ${(result.confidence * 100).toInt()}%", fontSize = 15.sp)
                        if (result.lesionAreaRatio > 0f) {
                            Text(
                                "病斑面积: ${"%.1f".format(result.lesionAreaRatio)}%",
                                fontSize = 15.sp
                            )
                            Text("病情级别: ${result.severityLevel}", fontSize = 15.sp)
                        }
                        if (result.top3.size > 1) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Top3: ${result.top3.joinToString { "${it.className}(${(it.confidence * 100).toInt()}%)" }}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // Error dialog
        if (uiState.cameraState == CameraState.ERROR && uiState.errorMessage != null) {
            AlertDialog(
                onDismissRequest = { viewModel.onDiscard() },
                title = { Text("提示") },
                text = { Text(uiState.errorMessage!!) },
                confirmButton = {
                    TextButton(onClick = { viewModel.onDiscard() }) { Text("确定") }
                }
            )
        }
    }

    // Custom leaf position dialog
    if (showAddDialog) {
        var newLeaf by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("添加叶片位") },
            text = {
                OutlinedTextField(
                    value = newLeaf,
                    onValueChange = { newLeaf = it },
                    label = { Text("叶片位名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addCustomLeafPosition(newLeaf)
                    viewModel.updateLeafPosition(newLeaf)
                    showAddDialog = false
                }) { Text("添加") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("取消") }
            }
        )
    }
}

private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
    return try {
        val planes = imageProxy.planes
        if (planes.size < 3) {
            // Fallback: use JPEG from buffer
            val buffer = planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else {
            // Proper YUV_420_888 → RGB conversion
            val yPlane = planes[0]
            val uPlane = planes[1]
            val vPlane = planes[2]

            val yBuffer = yPlane.buffer
            val uBuffer = uPlane.buffer
            val vBuffer = vPlane.buffer

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val yBytes = ByteArray(ySize)
            val uBytes = ByteArray(uSize)
            val vBytes = ByteArray(vSize)

            yBuffer.get(yBytes)
            uBuffer.get(uBytes)
            vBuffer.get(vBytes)

            val w = imageProxy.width
            val h = imageProxy.height
            val yRowStride = yPlane.rowStride
            val uvRowStride = uPlane.rowStride
            val uvPixelStride = uPlane.pixelStride

            val rgb = IntArray(w * h)
            for (j in 0 until h) {
                for (i in 0 until w) {
                    val y = yBytes[j * yRowStride + i].toInt() and 0xFF
                    val u = if (uvPixelStride == 1) {
                        uBytes[j * uvRowStride + i].toInt() and 0xFF
                    } else {
                        uBytes[(j / 2) * uvRowStride + (i / 2) * uvPixelStride].toInt() and 0xFF
                    }
                    val v = if (uvPixelStride == 1) {
                        vBytes[j * uvRowStride + i].toInt() and 0xFF
                    } else {
                        vBytes[(j / 2) * uvRowStride + (i / 2) * uvPixelStride].toInt() and 0xFF
                    }

                    val yVal = y - 16
                    val uVal = u - 128
                    val vVal = v - 128

                    var r = (1.164f * yVal + 1.596f * vVal).toInt()
                    var g = (1.164f * yVal - 0.813f * vVal - 0.391f * uVal).toInt()
                    var b = (1.164f * yVal + 2.018f * uVal).toInt()

                    r = r.coerceIn(0, 255)
                    g = g.coerceIn(0, 255)
                    b = b.coerceIn(0, 255)

                    rgb[j * w + i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                }
            }

            Bitmap.createBitmap(rgb, w, h, Bitmap.Config.ARGB_8888)
        }
    } catch (e: Exception) {
        null
    }
}
