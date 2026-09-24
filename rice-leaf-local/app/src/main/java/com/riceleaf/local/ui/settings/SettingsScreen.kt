package com.riceleaf.local.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
    ) {
        TopAppBar(title = { Text("设置") })

        // Model selection
        SettingsGroup("模型选择") {
            if (uiState.installedModels.isEmpty()) {
                Text(
                    "暂未安装模型，请将 .onnx 文件和 _info.json 文件放入 assets/models/ 目录",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            } else {
                uiState.installedModels.forEach { model ->
                    val isSelected = model.name == uiState.currentModelName
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp)
                            .clickable { viewModel.selectModel(model.name) },
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.selectModel(model.name) }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(model.name, fontWeight = FontWeight.Bold)
                                Text(
                                    "v${model.version} | 输入: ${model.inputSize[0]}×${model.inputSize[1]} | ${model.classes.size}类",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        Divider(modifier = Modifier.padding(horizontal = 16.dp))

        // About
        SettingsGroup("关于") {
            SettingsItem("应用版本", "v2.0.0")
            SettingsItem("模型框架", "ONNX Runtime Mobile")
            SettingsItem("推理引擎", "ResNet50 (本地推理)")
        }

        Divider(modifier = Modifier.padding(horizontal = 16.dp))

        // Disease reference
        SettingsGroup("病害对照表") {
            val diseases = listOf(
                "健康" to "叶片无病斑，颜色正常",
                "叶烫病" to "水渍状斑点，褐色不规则斑块",
                "白叶枯病" to "叶尖黄白色条纹，灰白色枯斑",
                "稻瘟病" to "梭形褐色病斑，中央灰白",
                "穗颈瘟" to "穗颈和枝梗褐色坏死，导致白穗秕粒",
                "窄褐斑病" to "窄长形褐色斑点，沿叶脉分布",
                "纹枯病" to "云纹状褐色斑块，湿度大见菌丝",
                "胡麻叶斑病" to "圆形褐色小斑点，形似胡麻"
            )
            diseases.forEach { (name, desc) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(name, fontWeight = FontWeight.Bold, modifier = Modifier.width(90.dp))
                    Text(desc, fontSize = 13.sp, color = Color.Gray)
                }
            }
        }

        Divider(modifier = Modifier.padding(horizontal = 16.dp))

        // Severity levels
        SettingsGroup("病情级别") {
            val levels = listOf(
                "0级" to "病斑面积 0% (健康)",
                "1级" to "病斑面积 0.1% ~ 5.0%",
                "3级" to "病斑面积 5.1% ~ 10.0%",
                "5级" to "病斑面积 10.1% ~ 25.0%",
                "7级" to "病斑面积 25.1% ~ 50.0%",
                "9级" to "病斑面积 50.1% ~ 100%"
            )
            levels.forEach { (level, desc) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(level, fontWeight = FontWeight.Bold, modifier = Modifier.width(70.dp))
                    Text(desc, fontSize = 13.sp, color = Color.Gray)
                }
            }
        }

        Divider(modifier = Modifier.padding(horizontal = 16.dp))

        // Usage guide
        SettingsGroup("使用说明") {
            Text(
                "1. 将叶片放置在深色背景下，确保光照充足\n" +
                        "2. 点击中间⭕按钮拍照\n" +
                        "3. 输入株号，选择叶片位\n" +
                        "4. 点击✓确认进行识别\n" +
                        "5. 点击✕丢弃重拍\n" +
                        "6. 识别结果以顶部弹窗显示",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 13.sp,
                color = Color.Gray,
                lineHeight = 22.sp
            )
        }

        Divider(modifier = Modifier.padding(horizontal = 16.dp))

        // Data management
        SettingsGroup("数据管理") {
            var showClearDialog by remember { mutableStateOf(false) }
            TextButton(onClick = { showClearDialog = true }) {
                Text("清空所有数据", color = MaterialTheme.colorScheme.error)
            }
            if (showClearDialog) {
                AlertDialog(
                    onDismissRequest = { showClearDialog = false },
                    title = { Text("确认清空") },
                    text = { Text("清空将删除所有记录和图片，不可恢复。确定继续？") },
                    confirmButton = {
                        TextButton(onClick = { showClearDialog = false }) {
                            Text("确认", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showClearDialog = false }) { Text("取消") }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

@Composable
private fun SettingsItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(value, color = Color.Gray)
    }
}
