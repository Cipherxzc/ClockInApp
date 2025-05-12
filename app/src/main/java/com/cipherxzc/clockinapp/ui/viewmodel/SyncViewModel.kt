package com.cipherxzc.clockinapp.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.cipherxzc.clockinapp.data.repository.CloudRepository
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SyncViewModel(
    application: Application,
    private val databaseViewModel: DatabaseViewModel
) : AndroidViewModel(application) {
    companion object {
        private const val PREFS_NAME = "sync_prefs"
        private const val KEY_LAST_SYNC = "last_sync_millis"
    }

    private val prefs = application.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val cloudRepo: CloudRepository by lazy {
        CloudRepository(FirebaseFirestore.getInstance())
    }

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    private fun loadLastSync(): Timestamp {
        val millis = prefs.getLong(KEY_LAST_SYNC, 0L)
        val seconds = millis / 1000
        val nanos = ((millis % 1000) * 1_000_000).toInt()
        return Timestamp(seconds, nanos)
    }

    private fun saveLastSync(ts: Timestamp) {
        val millis = ts.seconds * 1000 + ts.nanoseconds / 1_000_000
        prefs.edit() { putLong(KEY_LAST_SYNC, millis) }
    }

    fun sync(
        onComplete: (() -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            try {
                val since = loadLastSync()
                val userId = databaseViewModel.getCurrentUser()
                val now = Timestamp.now()

                // 拉取云端增量 Item & Record
                val updatedItems   = cloudRepo.fetchUpdatedItems(userId, since)
                databaseViewModel.upsertItems(updatedItems)

                val updatedRecords = cloudRepo.fetchUpdatedRecords(userId, since, updatedItems)
                databaseViewModel.upsertRecords(updatedRecords)

                // 推送本地未同步的 Item & Record
                val unsyncedItems   = databaseViewModel.getUnsyncedItems()
                cloudRepo.pushItems(userId, unsyncedItems)
                databaseViewModel.upsertItems(unsyncedItems)

                val unsyncedRecords = databaseViewModel.getUnsyncedRecords()
                cloudRepo.pushRecords(userId, unsyncedRecords)
                databaseViewModel.upsertRecords(unsyncedRecords)

                // 更新本地最后同步时间
                saveLastSync(now)

                withContext(Dispatchers.Main) {
                    onComplete?.invoke()
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    onError?.invoke(e)
                }
            } finally {
                _isSyncing.value = false
            }
        }
    }
}

class SyncViewModelFactory(
    private val application: Application,
    private val databaseViewModel: DatabaseViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == SyncViewModel::class.java)
        return SyncViewModel(application, databaseViewModel) as T
    }
}