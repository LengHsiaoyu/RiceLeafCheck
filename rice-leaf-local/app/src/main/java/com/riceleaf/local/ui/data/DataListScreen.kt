package com.riceleaf.local.ui.data

import androidx.compose.foundation.clickable
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.riceleaf.local.data.local.entity.RecordEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataListScreen(
    viewModel: DataViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) { viewModel.loadMore() }

    LaunchedEffect(listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index) {
        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        if (lastVisible >= uiState.records.size - 3) {
            viewModel.loadMore()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("检测记录") },
                actions = {
                    if (uiState.records.isNotEmpty()) {
                        IconButton(onClick = { viewModel.exportToExcel() }) {
                            Icon(Icons.Default.Share, contentDescription = "导出")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("加载失败", color = MaterialTheme.colorScheme.error)
                    Text(uiState.errorMessage!!, fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { viewModel.loadMore() }) { Text("重试") }
                }
            }
        } else if (uiState.records.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无记录", color = Color.Gray)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.records, key = { it.id }) { record ->
                    RecordCard(record = record, onClick = { viewModel.selectRecord(record) })
                }
                if (uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                }
            }
        }
    }

    // Detail dialog
    uiState.selectedRecord?.let { record ->
        DataDetailDialog(
            record = record,
            onDismiss = { viewModel.clearSelection() },
            onDelete = { viewModel.deleteRecord(record.id) },
            onUpdate = { viewModel.updateRecord(it) }
        )
    }

    // Export result
    uiState.exportResult?.let { path ->
        AlertDialog(
            onDismissRequest = { viewModel.clearExportResult() },
            title = { Text("导出成功") },
            text = { Text("文件已保存到:\n$path") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearExportResult() }) { Text("确定") }
            }
        )
    }
}

@Composable
private fun RecordCard(record: RecordEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LocalImage(
                path = record.imagePath,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    record.diseaseName ?: "未识别",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("株号: ${record.plantNo}", fontSize = 13.sp, color = Color.Gray)
                    Text("叶片位: ${record.leafPosition}", fontSize = 13.sp, color = Color.Gray)
                }
                record.confidence?.let {
                    Text(
                        "置信度: ${(it * 100).toInt()}%",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    formatTime(record.createTime),
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }
        }
    }
}

@Composable
private fun DataDetailDialog(
    record: RecordEntity,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (RecordEntity) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var editingRemark by remember { mutableStateOf(record.remark) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("记录详情") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LocalImage(
                    path = record.imagePath,
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentScale = ContentScale.Fit
                )
                Text("株号: ${record.plantNo}")
                Text("叶片位: ${record.leafPosition}")
                Text("病害: ${record.diseaseName ?: "未识别"}")
                record.confidence?.let {
                    Text("置信度: ${(it * 100).toInt()}%")
                }
                record.lesionAreaRatio?.let {
                    if (it > 0f) {
                        Text("病斑面积: ${"%.1f".format(it)}%")
                        Text("病情级别: ${record.severityLevel}")
                    }
                }
                record.symptomDesc?.let {
                    if (it.isNotBlank()) {
                        Text("症状: $it", fontSize = 14.sp, color = Color.Gray)
                    }
                }
                Text("时间: ${formatTime(record.createTime)}")
                OutlinedTextField(
                    value = editingRemark,
                    onValueChange = { editingRemark = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onUpdate(record.copy(remark = editingRemark))
                onDismiss()
            }) { Text("保存备注") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { showDeleteConfirm = true }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) { Text("关闭") }
            }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("删除后无法恢复，确定要删除这条记录吗？") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                    onDismiss()
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") }
            }
        )
    }
}

private fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}

@Composable
private fun LocalImage(
    path: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    var bitmap by remember(path) { mutableStateOf<android.graphics.Bitmap?>(null) }
    var error by remember(path) { mutableStateOf(false) }

    LaunchedEffect(path) {
        try {
            val file = File(path)
            if (file.exists()) {
                val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
                bitmap = BitmapFactory.decodeFile(path, opts)
                if (bitmap == null) error = true
            } else {
                error = true
            }
        } catch (_: Exception) {
            error = true
        }
    }

    if (error) {
        Box(
            modifier = modifier.background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Text("无图片", fontSize = 12.sp, color = Color.Gray)
        }
    } else if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(
            modifier = modifier.background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        }
    }
}
