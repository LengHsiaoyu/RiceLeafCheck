package com.riceleaf.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.riceleaf.app.AppSettings
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var resolution by remember { mutableStateOf("高(1080p)") }
    var flashMode by remember { mutableStateOf("自动") }
    var saveOriginal by remember { mutableStateOf(true) }
    var exportFormat by remember { mutableStateOf("Excel") }
    val modelType by AppSettings.modelType.collectAsState()
    var localModelName by remember { mutableStateOf("未选择模型文件") }
    var showFileImported by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileName = uri.lastPathSegment ?: "model.tflite"
                val outFile = java.io.File(context.filesDir, "models/$fileName")
                outFile.parentFile?.mkdirs()
                inputStream?.use { input ->
                    FileOutputStream(outFile).use { output -> input.copyTo(output) }
                }
                localModelName = fileName
                showFileImported = true
            } catch (_: Exception) {
                localModelName = "导入失败"
            }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopAppBar(title = { Text("设置") })

        Section("识别模型") {
            Column(Modifier.selectableGroup()) {
                Row(Modifier.fillMaxWidth().selectable(
                    selected = modelType == "cloud", onClick = { AppSettings.setModelType("cloud") }, role = Role.RadioButton
                ).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = modelType == "cloud", onClick = null)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("云端模型", style = MaterialTheme.typography.bodyLarge)
                        Text("使用服务器云端 API 进行识别", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(Modifier.fillMaxWidth().selectable(
                    selected = modelType == "local", onClick = { AppSettings.setModelType("local") }, role = Role.RadioButton
                ).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = modelType == "local", onClick = null)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("本地模型", style = MaterialTheme.typography.bodyLarge)
                        Text("使用本地 TFLite 模型文件", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (modelType == "local") {
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(localModelName, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Button(onClick = { filePicker.launch(arrayOf("application/octet-stream", "*/*")) }) { Text("导入模型") }
                }
                if (showFileImported) {
                    Text("模型文件已导入", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        HorizontalDivider()
        Section("拍照参数") {
            Dropdown("分辨率", resolution, listOf("高(1080p)", "中(720p)", "低(480p)")) { resolution = it }
            Dropdown("闪光灯", flashMode, listOf("自动", "开启", "关闭")) { flashMode = it }
            Switch("保存原图到本地", saveOriginal) { saveOriginal = it }
        }
        HorizontalDivider()
        Section("数据管理") {
            Dropdown("导出格式", exportFormat, listOf("Excel", "CSV")) { exportFormat = it }
            TextButton(onClick = {}, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("清空本地缓存") }
        }
        HorizontalDivider()
        Section("关于") {
            ListItem(headlineContent = { Text("版本号") }, supportingContent = { Text("v1.0.0") })
            ListItem(headlineContent = { Text("使用说明") }, supportingContent = { Text("点击查看") })
            ListItem(headlineContent = { Text("病害对照表") }, supportingContent = { Text("点击查看") })
        }
    }
}

@Composable
fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
        content()
    }
}

@Composable
fun Dropdown(label: String, value: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Box {
            TextButton(onClick = { expanded = true }) { Text(value) }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { onSelect(it); expanded = false }) }
            }
        }
    }
}

@Composable
fun Switch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
