package com.cipherxzc.clockinapp.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.cipherxzc.clockinapp.data.database.AppDatabase
import com.cipherxzc.clockinapp.data.database.ClockInItem
import com.cipherxzc.clockinapp.data.database.ClockInRecord
import com.cipherxzc.clockinapp.data.repository.LocalRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.core.content.edit

// layout: /users/{userId}/items/{itemId}/records/{recordId}
class SyncViewModel(
    application: Application,
    private val databaseViewModel: DatabaseViewModel,   // injected from UI layer
) : AndroidViewModel(application) {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    // 增量下拉
    private val prefs = application.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
    private val PREF_LAST_SYNC = "last_sync_ts"

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            "clock-in_database"
        ).build()
    }
    private val localRepo: LocalRepository by lazy { LocalRepository(database) }

    fun sync() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isSyncing.value) return@launch // ignore duplicate taps
            _isSyncing.value = true
            try {
                performSync()
            } finally {
                _isSyncing.value = false
            }
        }
    }

    private suspend fun performSync() {
        val userId = databaseViewModel.getCurrentUser()
        val lastSyncTs = getLastSyncTimestamp()

        // 1. Pull: cloud->local
        pullUpdatedItems(userId, lastSyncTs)
        pullUpdatedRecords(userId, lastSyncTs)

        // 2. Push: local->cloud
        pushUnsyncedItems(userId)
        pushUnsyncedRecords(userId)

        // 3. Update last sync timestamp
        saveLastSyncTimestamp(Timestamp.now())
    }

    private suspend fun pullUpdatedItems(userId: String, lastSync: Timestamp) {
        val itemsRef = userItemsRef(userId).whereGreaterThan("lastModified", lastSync)
        val snapshot = itemsRef.get().await()

        if (snapshot.isEmpty) return

        val entities = snapshot.documents.mapNotNull { doc ->
            try {
                doc.toObject(ClockInItem::class.java)?.copy(isSynced = true)
            } catch (_: Exception) { null }  // TODO
        }
        localRepo.upsertItems(entities)
    }

    private suspend fun pullUpdatedRecords(userId: String, lastSync: Timestamp) {
        // NOTE: record sub‑collections must be queried individually per item.
        // For brevity we use a collectionGroup query (requires enabled indexes).
        val recordsQuery = firestore.collectionGroup("records")
            .whereEqualTo("userId", userId)
            .whereGreaterThan("lastModified", lastSync)
        val snapshot = recordsQuery.get().await()
        if (snapshot.isEmpty) return

        val entities = snapshot.documents.mapNotNull { doc ->
            try {
                doc.toObject(ClockInRecord::class.java)?.copy(isSynced = true)
            } catch (_: Exception) { null }
        }
        localRepo.upsertRecords(entities)
    }

    /* ------------------------------------------------------- */
    /* ---------------  STEP 2: PUSH  ------------------------- */
    /* ------------------------------------------------------- */

    private suspend fun pushUnsyncedItems(userId: String) {
        val unsynced = localRepo.getUnsyncedItems(userId)
        if (unsynced.isEmpty()) return

        unsynced.forEach { entity ->
            val docRef = userItemsRef(userId).document(entity.itemId)
            val toUpload = entity.copy(
                lastModified = Timestamp.now(),
            )
            docRef.set(toUpload).await()
        }
        // Mark as synced locally
        unsynced.forEach { it.isSynced = true }
        localRepo.upsertItems(unsynced)
    }

    private suspend fun pushUnsyncedRecords(userId: String) {
        val unsynced = localRepo.getUnnsyncedRecords(userId)
        if (unsynced.isEmpty()) return

        unsynced.forEach { entity ->
            val recordRef: CollectionReference = userItemsRef(userId)
                .document(entity.itemId)
                .collection("records")
            val toUpload = entity.copy(lastModified = Timestamp.now())
            recordRef.document(entity.recordId).set(toUpload).await()
        }
        unsynced.forEach { it.isSynced = true }
        localRepo.upsertRecords(unsynced)
    }

    private fun userItemsRef(userId: String): CollectionReference =
        firestore.collection("users").document(userId).collection("items")

    private fun getLastSyncTimestamp(): Timestamp {
        val millis = prefs.getLong(PREF_LAST_SYNC, 0L)
        return Timestamp(millis / 1000, ((millis % 1000) * 1_000_000).toInt())
    }

    private fun saveLastSyncTimestamp(ts: Timestamp) {
        prefs.edit() { putLong(PREF_LAST_SYNC, ts.toDate().time) }
    }
}