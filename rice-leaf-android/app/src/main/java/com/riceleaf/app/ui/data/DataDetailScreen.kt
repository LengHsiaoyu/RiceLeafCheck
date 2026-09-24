package com.riceleaf.app.ui.data

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataDetailScreen(recordId: Long, onBack: () -> Unit, viewModel: DataViewModel = viewModel()) {
    val state by viewModel.detailState.collectAsState()
    LaunchedEffect(recordId) { viewModel.loadDetail(recordId) }
    var showDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("记录详情") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
                actions = { TextButton(onClick = { showDelete = true }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("删除") } })
        }
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.errorMessage != null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text(state.errorMessage!!) }
            state.record != null -> {
                val r = state.record!!
                Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailRow("样点编号", r.samplePoint); DetailRow("株号", r.plantNo); DetailRow("叶片位", r.leafPosition)
                    DetailRow("病害名称", r.diseaseName); DetailRow("置信度", "${((r.confidence ?: 0.0) * 100).toInt()}%")
                    DetailRow("病斑面积占比", "${r.lesionAreaRatio ?: "-"}%"); DetailRow("病情级别", r.severityLevel?.toString())
                    DetailRow("检测时间", r.createTime?.take(19)); r.symptomDesc?.let { DetailRow("症状描述", it) }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = state.remark, onValueChange = viewModel::updateRemark, label = { Text("备注") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    Button(onClick = { viewModel.saveRemark(recordId) }, enabled = !state.isSaving, modifier = Modifier.align(Alignment.End)) { Text(if (state.isSaving) "保存中..." else "保存备注") }
                }
            }
        }
    }

    if (showDelete) AlertDialog(
        onDismissRequest = { showDelete = false }, title = { Text("确认删除") }, text = { Text("确定要删除这条记录吗？") },
        confirmButton = { TextButton(onClick = { showDelete = false; viewModel.deleteRecord(recordId) { onBack() } }) { Text("删除", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = { showDelete = false }) { Text("取消") } }
    )
}

@Composable
fun DetailRow(label: String, value: String?) {
    Row(Modifier.fillMaxWidth()) {
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(120.dp))
        Text(value ?: "-", style = MaterialTheme.typography.bodyMedium)
    }
    HorizontalDivider()
}
