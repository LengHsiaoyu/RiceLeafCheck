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
fun DiseaseReferenceScreen(navController: NavController) {
    val diseases = listOf(
        Triple("稻瘟病（叶瘟）", "梭形或圆形斑，边缘褐色，中央灰白色", "潮湿时背面有灰色霉层"),
        Triple("纹枯病", "叶鞘及叶片上云纹状大斑，边缘深褐色", "病部可见白色菌丝团或褐色菌核"),
        Triple("白叶枯病", "沿叶缘或叶脉呈黄白色长条状，病部与健部界限呈波浪纹", "湿度大时表面有淡黄色菌脓"),
        Triple("细菌性条斑病", "细条状半透明水渍斑，对光看呈半透明", "干燥后呈白色，有无数小条斑"),
        Triple("胡麻叶斑病", "芝麻粒大小褐色至红褐色斑点，边缘黄色晕圈", "严重时连成不规则大斑")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("病害对照表") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            diseases.forEach { (name, symptom, spotFeature) ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("典型症状: $symptom", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("病斑特征: $spotFeature", fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "病情级别参照国标: 0=无病, 1=≤5%, 3=6~10%, 5=11~25%, 7=26~50%, 9=≥51%",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
