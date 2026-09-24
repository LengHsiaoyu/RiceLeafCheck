package com.riceleaf.app.ui.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riceleaf.app.network.RecordItem
import com.riceleaf.app.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DataListUiState(
    val records: List<RecordItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val currentPage: Int = 1,
    val errorMessage: String? = null
)

data class DetailUiState(
    val record: RecordItem? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val remark: String = "",
    val errorMessage: String? = null
)

class DataViewModel : ViewModel() {

    private val _listState = MutableStateFlow(DataListUiState())
    val listState: StateFlow<DataListUiState> = _listState

    private val _detailState = MutableStateFlow(DetailUiState())
    val detailState: StateFlow<DetailUiState> = _detailState

    init { loadRecords() }

    fun loadRecords() {
        _listState.value = _listState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.apiService.getRecords(page = 1)
                if (resp.code == 200 && resp.data != null) {
                    _listState.value = _listState.value.copy(
                        records = resp.data.records, isLoading = false,
                        hasMore = resp.data.records.size < resp.data.total, currentPage = resp.data.page
                    )
                } else {
                    _listState.value = _listState.value.copy(isLoading = false, errorMessage = resp.message ?: "加载失败")
                }
            } catch (e: Exception) {
                _listState.value = _listState.value.copy(isLoading = false, errorMessage = "网络错误: ${e.localizedMessage}")
            }
        }
    }

    fun loadMore() {
        val s = _listState.value
        if (s.isLoadingMore || !s.hasMore) return
        _listState.value = s.copy(isLoadingMore = true)
        viewModelScope.launch {
            try {
                val next = s.currentPage + 1
                val resp = RetrofitClient.apiService.getRecords(page = next)
                if (resp.code == 200 && resp.data != null) {
                    val all = s.records + resp.data.records
                    _listState.value = _listState.value.copy(
                        records = all, isLoadingMore = false,
                        hasMore = all.size < resp.data.total, currentPage = resp.data.page
                    )
                }
            } catch (_: Exception) {
                _listState.value = _listState.value.copy(isLoadingMore = false)
            }
        }
    }

    fun loadDetail(id: Long) {
        _detailState.value = DetailUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.apiService.getRecordDetail(id)
                if (resp.code == 200 && resp.data != null) {
                    _detailState.value = _detailState.value.copy(
                        record = resp.data, isLoading = false, remark = resp.data.remark ?: ""
                    )
                } else {
                    _detailState.value = DetailUiState(errorMessage = resp.message ?: "加载失败")
                }
            } catch (e: Exception) {
                _detailState.value = DetailUiState(errorMessage = "网络错误: ${e.localizedMessage}")
            }
        }
    }

    fun updateRemark(value: String) { _detailState.value = _detailState.value.copy(remark = value) }

    fun saveRemark(id: Long) {
        _detailState.value = _detailState.value.copy(isSaving = true)
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.updateRecord(id, mapOf("remark" to _detailState.value.remark))
                _detailState.value = _detailState.value.copy(isSaving = false)
            } catch (_: Exception) {
                _detailState.value = _detailState.value.copy(isSaving = false, errorMessage = "保存失败")
            }
        }
    }

    fun deleteRecord(id: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            try { RetrofitClient.apiService.deleteRecord(id); onDeleted() } catch (_: Exception) {}
        }
    }
}
