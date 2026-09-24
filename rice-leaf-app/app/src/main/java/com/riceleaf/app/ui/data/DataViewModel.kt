package com.riceleaf.app.ui.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riceleaf.app.data.remote.dto.RecordUpdateRequest
import com.riceleaf.app.data.repository.RecordRepository
import com.riceleaf.app.domain.model.RecordDetail
import com.riceleaf.app.domain.model.RecordSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DataListUiState(
    val records: List<RecordSummary> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val total: Long = 0
)

data class DataDetailUiState(
    val record: RecordDetail? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val editMode: Boolean = false,
    val editRemark: String = "",
    val editSeverityLevel: Int = 0
)

@HiltViewModel
class DataViewModel @Inject constructor(
    private val repository: RecordRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(DataListUiState())
    val listState: StateFlow<DataListUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow(DataDetailUiState())
    val detailState: StateFlow<DataDetailUiState> = _detailState.asStateFlow()

    fun loadRecords(refresh: Boolean = false) {
        val current = _listState.value
        if (current.isLoading) return

        val page = if (refresh) 1 else current.currentPage
        _listState.value = current.copy(isLoading = true, isRefreshing = refresh, error = null)

        viewModelScope.launch {
            repository.getRecords(page = page)
                .onSuccess { (records, total) ->
                    val allRecords = if (refresh) records
                        else _listState.value.records + records
                    _listState.value = _listState.value.copy(
                        records = allRecords,
                        isLoading = false,
                        isRefreshing = false,
                        currentPage = page + 1,
                        hasMore = allRecords.size < total,
                        total = total
                    )
                }
                .onFailure { e ->
                    _listState.value = _listState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.message ?: "加载失败"
                    )
                }
        }
    }

    fun loadDetail(recordId: Long) {
        _detailState.value = DataDetailUiState(isLoading = true)
        viewModelScope.launch {
            repository.getRecordDetail(recordId)
                .onSuccess { record ->
                    _detailState.value = DataDetailUiState(
                        record = record,
                        editRemark = record.remark,
                        editSeverityLevel = record.severityLevel
                    )
                }
                .onFailure { e ->
                    _detailState.value = DataDetailUiState(
                        error = e.message ?: "加载失败"
                    )
                }
        }
    }

    fun enterEditMode() {
        val record = _detailState.value.record ?: return
        _detailState.value = _detailState.value.copy(
            editMode = true,
            editRemark = record.remark,
            editSeverityLevel = record.severityLevel
        )
    }

    fun updateEditRemark(value: String) {
        _detailState.value = _detailState.value.copy(editRemark = value)
    }

    fun updateEditSeverityLevel(value: Int) {
        _detailState.value = _detailState.value.copy(editSeverityLevel = value)
    }

    fun saveRecord() {
        val state = _detailState.value
        val record = state.record ?: return

        _detailState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            repository.updateRecord(
                record.id,
                RecordUpdateRequest(
                    samplePoint = record.samplePoint,
                    plantNo = record.plantNo,
                    leafPosition = record.leafPosition,
                    diseaseId = null,
                    lesionAreaRatio = record.lesionAreaRatio,
                    severityLevel = state.editSeverityLevel,
                    remark = state.editRemark
                )
            ).onSuccess {
                loadDetail(record.id)
                _detailState.value = _detailState.value.copy(
                    editMode = false,
                    isSaving = false
                )
            }.onFailure { e ->
                _detailState.value = _detailState.value.copy(
                    isSaving = false,
                    error = e.message
                )
            }
        }
    }

    fun deleteRecord(recordId: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteRecord(recordId)
                .onSuccess { onDeleted() }
                .onFailure { e ->
                    _detailState.value = _detailState.value.copy(
                        error = e.message ?: "删除失败"
                    )
                }
        }
    }
}
