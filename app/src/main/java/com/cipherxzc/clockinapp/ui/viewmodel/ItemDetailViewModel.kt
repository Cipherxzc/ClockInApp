package com.cipherxzc.clockinapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cipherxzc.clockinapp.data.database.ClockInItem
import com.cipherxzc.clockinapp.data.database.ClockInRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ItemDetailViewModel(
    private val databaseViewModel: DatabaseViewModel
) : ViewModel() {

    private val _itemFlow = MutableStateFlow<ClockInItem?>(null)
    val itemFlow: StateFlow<ClockInItem?> = _itemFlow

    private val _recordsFlow = MutableStateFlow<List<ClockInRecord>>(emptyList())
    val recordsFlow: StateFlow<List<ClockInRecord>> = _recordsFlow

    private val _isClockedInTodayFlow = MutableStateFlow(false)
    val isClockedInTodayFlow: StateFlow<Boolean> = _isClockedInTodayFlow

    private val _isLoadingFlow = MutableStateFlow(false)
    val isLoadingFlow: StateFlow<Boolean> = _isLoadingFlow

    fun loadDetail(itemId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingFlow.value = true
            _itemFlow.value = databaseViewModel.getItem(itemId)
            _recordsFlow.value = databaseViewModel.getAllRecords(itemId)
            _isClockedInTodayFlow.value = databaseViewModel.isClockedInToday(itemId)
            _isLoadingFlow.value = false
        }
    }
}

class ItemDetailViewModelFactory(
    private val databaseViewModel: DatabaseViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ItemDetailViewModel::class.java)
        return ItemDetailViewModel(databaseViewModel) as T
    }
}