package com.cipherxzc.clockinapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cipherxzc.clockinapp.data.database.ClockInItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ItemListViewModelFactory(
    private val databaseViewModel: DatabaseViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ItemListViewModel::class.java)
        return ItemListViewModel(databaseViewModel) as T
    }
}

class ItemListViewModel(
    private val databaseViewModel: DatabaseViewModel
) : ViewModel() {

    private val _clockedInItemsFlow =
        MutableStateFlow<List<ClockInItem>>(emptyList())
    val clockedInItemsFlow: StateFlow<List<ClockInItem>> = _clockedInItemsFlow

    private val _unClockedInItemsFlow =
        MutableStateFlow<List<ClockInItem>>(emptyList())
    val unClockedInItemsFlow: StateFlow<List<ClockInItem>> = _unClockedInItemsFlow

    private val _showDialogFlow = MutableStateFlow(false)
    val showDialogFlow: StateFlow<Boolean> = _showDialogFlow

    private val _isLoadingFlow = MutableStateFlow(false)
    val isLoadingFlow: StateFlow<Boolean> = _isLoadingFlow

    init {
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingFlow.value = true
            val allItems = databaseViewModel.getAllItems()

            // 分组并创建新的 List
            val clockedIn = allItems.filter { databaseViewModel.isClockedInToday(it.itemId) }
            val notClockedIn = allItems.filterNot { databaseViewModel.isClockedInToday(it.itemId) }

            _clockedInItemsFlow.value = clockedIn
            _unClockedInItemsFlow.value = notClockedIn
            _isLoadingFlow.value = false
        }
    }

    fun insertItem(name: String, description: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val newItem = databaseViewModel.insertItem(name, description)
            loadItems()
            // _unClockedInItemsFlow.value = _unClockedInItemsFlow.value + newItem
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseViewModel.deleteItem(itemId)
            loadItems()
        }
    }

    fun showDialog()  = _showDialogFlow.update { true }
    fun hideDialog()  = _showDialogFlow.update { false }

    fun clockIn(itemId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseViewModel.insertRecord(itemId)
            loadItems()
        }
    }

    fun withdraw(itemId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseViewModel.deleteMostRecentRecord(itemId)
            loadItems()
        }
    }
}