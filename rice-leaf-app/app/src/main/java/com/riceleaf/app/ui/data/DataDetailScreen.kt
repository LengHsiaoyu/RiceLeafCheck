package com.riceleaf.app.ui.data

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.riceleaf.app.BuildConfig
import com.riceleaf.app.domain.model.RecordDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataDetailScreen(
    navController: NavController,
    recordId: Long,
    viewModel: DataViewModel = hiltViewModel()
) {
    val uiState by viewModel.detailState.collectAsState()

    LaunchedEffect(recordId) {
        viewModel.loadDetail(recordId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("叶片详情") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    if (!uiState.editMode) {
                        IconButton(onClick = viewModel::enterEditMode) {
                            Icon(Icons.Default.Edit, "修改")
                        }
                        var showDelete by remember { mutableStateOf(false) }
                        IconButton(onClick = { showDelete = true }) {
                            Icon(Icons.Default.Delete, "删除")
                        }
                        AlertDialog(
                            onDismissRequest = { showDelete = false },
                            title = { Text("确认删除") },
                            text = { Text("删除该检测记录？此操作不可恢复。") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDelete = false
                                    viewModel.deleteRecord(recordId) {
                                        navController.popBackStack()
                                    }
                                }) { Text("删除") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDelete = false }) { Text("取消") }
                            }
                        )
                    } else {
                        TextButton(
                            onClick = viewModel::saveRecord,
                            enabled = !uiState.isSaving
                        ) { Text("保存") }
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.record != null) {
            val record = uiState.record!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Image
                AsyncImage(
                    model = if (record.imageUrl.isNotEmpty())
                        BuildConfig.SERVER_BASE_URL.replace("/api/v1/", "") + record.imageUrl
                    else null,
                    contentDescription = "叶片原图",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    // Info card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            DetailRow("样点", record.samplePoint)
                            DetailRow("株号", record.plantNo)
                            DetailRow("叶片位", record.leafPosition)
                            DetailRow("病害名称", record.diseaseName)
                            DetailRow("病斑面积占比", "${record.lesionAreaRatio}%")
                            DetailRow("置信度", "${(record.confidence * 100).toInt()}%")

                            if (uiState.editMode) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("病情级别 (0-9)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                var severityText by remember { mutableStateOf(uiState.editSeverityLevel.toString()) }
                                OutlinedTextField(
                                    value = severityText,
                                    onValueChange = {
                                        severityText = it
                                        it.toIntOrNull()?.let { level ->
                                            if (level in 0..9) viewModel.updateEditSeverityLevel(level)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            } else {
                                DetailRow("病情级别", "级别 ${record.severityLevel}")
                            }

                            DetailRow("症状描述", record.symptomDesc)

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("备注", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))

                            if (uiState.editMode) {
                                OutlinedTextField(
                                    value = uiState.editRemark,
                                    onValueChange = viewModel::updateEditRemark,
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3
                                )
                            } else {
                                Text(
                                    record.remark.ifEmpty { "无" },
                                    fontSize = 14.sp,
                                    color = if (record.remark.isEmpty())
                                        MaterialTheme.colorScheme.onSurfaceVariant else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Time info
                    Text(
                        "记录时间: ${record.createTime}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "修改时间: ${record.updateTime}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(value, fontSize = 14.sp)
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
}
