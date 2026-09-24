package com.riceleaf.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageGuideScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("使用说明") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GuideStep("1. 拍照", "进入拍照页面，将叶片放在取景框内，点击拍摄按钮拍照。")
            GuideStep("2. 填写参数", "拍照完成后，填写样点编号、株号和叶片位（倒1叶/倒2叶/倒3叶），点击上传识别。")
            GuideStep("3. 查看结果", "系统自动识别病害并返回结果，包括病害名称、置信度、病斑面积占比和病情级别。")
            GuideStep("4. 继续拍摄", "点击「继续拍摄」可以连续拍摄，叶片位会自动递推（倒3→倒2→倒1），株号也会自动递增。所有参数均可手动修改。")
            GuideStep("5. 数据管理", "在数据页面查看历史记录，点击记录可查看详情。支持修改备注、删除记录、导出 Excel 报表。")
            GuideStep("6. 病害对照", "在设置页面可查看病害对照表，包含 5 种常见水稻叶片病害的症状和病斑特征，方便田间快速比对。")

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("注意事项", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("- 拍摄时确保叶片完整、对焦清晰", fontSize = 14.sp)
                    Text("- 尽量在自然光下拍摄，避免强光直射", fontSize = 14.sp)
                    Text("- 每次拍摄前请确认样点和株号正确", fontSize = 14.sp)
                    Text("- 系统初期使用模拟识别，后续将升级为 AI 模型", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun GuideStep(title: String, description: String) {
    Column {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
