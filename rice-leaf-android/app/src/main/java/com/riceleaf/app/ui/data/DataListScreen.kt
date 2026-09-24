package com.riceleaf.app.ui.data

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.riceleaf.app.network.RetrofitClient
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataListScreen(
    onItemClick: (Long) -> Unit,
    viewModel: DataViewModel = viewModel()
) {
    val state by viewModel.listState.collectAsState()
    val listState = rememberLazyListState()
    val shouldLoadMore = remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= state.records.size - 3 && state.hasMore && !state.isLoadingMore
        }
    }
    LaunchedEffect(shouldLoadMore.value) { if (shouldLoadMore.value) viewModel.loadMore() }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("历史记录") }, actions = { TextButton(onClick = {}) { Text("导出") } })
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { viewModel.loadRecords() }) { Text("重试") }
                }
            }
            else -> LazyColumn(state = listState, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.records, key = { it.id }) { record ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onItemClick(record.id) }) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(modifier = Modifier.size(72.dp), shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
                                val imgUrl = RetrofitClient.imageUrl(record.thumbnailUrl)
                                if (imgUrl != null) {
                                    AsyncImage(model = imgUrl, contentDescription = "缩略图",
                                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                } else {
                                    Box(contentAlignment = Alignment.Center) { Text("无图", style = MaterialTheme.typography.bodySmall) }
                                }
                            }
                            Column {
                                Text(record.diseaseName ?: "未知", fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${record.leafPosition ?: ""} | 级别 ${record.severityLevel ?: "-"} | ${record.createTime?.take(10) ?: ""}",
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                if (state.isLoadingMore) item { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.padding(16.dp)) } }
            }
        }
    }
}
