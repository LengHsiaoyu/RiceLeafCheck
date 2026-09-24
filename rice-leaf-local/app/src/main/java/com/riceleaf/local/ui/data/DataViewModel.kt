package com.riceleaf.local.ui.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riceleaf.local.data.local.entity.RecordEntity
import com.riceleaf.local.data.repository.PhotoRepository
import com.riceleaf.local.util.ExcelExportUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DataUiState(
    val records: List<RecordEntity> = emptyList(),
    val isLoading: Boolean = false,
    val hasMore: Boolean = true,
    val selectedRecord: RecordEntity? = null,
    val exportResult: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class DataViewModel @Inject constructor(
    private val repository: PhotoRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(DataUiState())
    val uiState: StateFlow<DataUiState> = _uiState

    private var currentPage = 0
    private val pageSize = 20

    fun loadMore() {
        if (_uiState.value.isLoading || !_uiState.value.hasMore) return
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val items = repository.getPagedRecords(pageSize, currentPage * pageSize)
                currentPage++
                _uiState.value = _uiState.value.copy(
                    records = _uiState.value.records + items,
                    isLoading = false,
                    hasMore = items.size == pageSize
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "加载失败"
                )
            }
        }
    }

    fun selectRecord(record: RecordEntity) {
        _uiState.value = _uiState.value.copy(selectedRecord = record)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedRecord = null)
    }

    fun updateRecord(record: RecordEntity) {
        viewModelScope.launch {
            repository.updateRecord(record)
            _uiState.value = _uiState.value.copy(
                records = _uiState.value.records.map { if (it.id == record.id) record else it },
                selectedRecord = record
            )
        }
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteRecord(id)
            _uiState.value = _uiState.value.copy(
                records = _uiState.value.records.filter { it.id != id },
                selectedRecord = null
            )
        }
    }

    fun exportToExcel() {
        viewModelScope.launch {
            try {
                val records = repository.getAllForExport()
                val path = ExcelExportUtil.export(context, records)
                _uiState.value = _uiState.value.copy(exportResult = path)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(exportResult = null)
            }
        }
    }

    fun clearExportResult() {
        _uiState.value = _uiState.value.copy(exportResult = null)
    }
}
