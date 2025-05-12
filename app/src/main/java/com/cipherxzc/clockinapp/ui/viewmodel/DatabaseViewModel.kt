package com.cipherxzc.clockinapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.cipherxzc.clockinapp.data.database.AppDatabase
import com.cipherxzc.clockinapp.data.database.ClockInItem
import com.cipherxzc.clockinapp.data.database.ClockInRecord
import com.cipherxzc.clockinapp.data.repository.LocalRepository
import com.google.android.play.integrity.internal.u
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId

class DatabaseViewModel(application: Application) : AndroidViewModel(application) {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            "clock-in_database"
        ).build()
    }

    private val localRepo: LocalRepository by lazy {
        LocalRepository(database)
    }

    private var currentUserId: String? = null

    fun setCurrentUser(userId: String) {
        currentUserId = userId
    }

    fun resetCurrentUser() {
        currentUserId = null
    }

    fun getCurrentUser(): String {
        return currentUserId?: throw IllegalStateException("Current user ID is not set")
    }

    suspend fun insertItem(name: String, description: String?, userId: String? = currentUserId): ClockInItem {
        if (userId == null) {
            throw IllegalStateException("Current user ID is not set")
        }
        return localRepo.insertItem(userId, name, description)
    }

    suspend fun insertRecord(itemId: String, userId: String? = currentUserId): ClockInRecord {
        if (userId == null) {
            throw IllegalStateException("Current user ID is not set")
        }
        return localRepo.insertRecord(userId, itemId)
    }

    fun insertDefaultData(userId: String? = currentUserId, onComplete: (() -> Unit)? = null) {
        if (userId == null) {
            throw IllegalStateException("Current user ID is not set")
        }
        viewModelScope.launch(Dispatchers.IO) {
            localRepo.insertDefaultData(userId)

            onComplete?.let {
                withContext(Dispatchers.Main) {
                    it()
                }
            }
        }
    }

    suspend fun deleteItem(itemId: String) = localRepo.deleteItem(itemId)
    suspend fun deleteRecord(recordId: String) = localRepo.deleteRecord(recordId)

    suspend fun deleteMostRecentRecord(itemId: String, userId: String? = currentUserId) {
        if (userId == null) {
            throw IllegalStateException("Current user ID is not set")
        }
        localRepo.deleteMostRecentRecord(userId, itemId)
    }

    suspend fun getItem(itemId: String): ClockInItem? {
        return localRepo.getItemById(itemId)
    }

    suspend fun getAllItems(userId: String? = currentUserId): List<ClockInItem> {
        if (userId == null) {
            throw IllegalStateException("Current user ID is not set")
        }
        return localRepo.getItemsByUser(userId)
    }

    suspend fun getAllRecords(itemId: String, userId: String? = currentUserId): List<ClockInRecord> {
        if (userId == null) {
            throw IllegalStateException("Current user ID is not set")
        }
        return localRepo.getRecordsByItem(userId, itemId)
    }

    suspend fun isClockedInToday(itemId: String, userId: String? = currentUserId): Boolean {
        if (userId == null) {
            throw IllegalStateException("Current user ID is not set")
        }
        val today = LocalDate.now(ZoneId.systemDefault())
        return localRepo.hasClockInOnDay(userId, itemId, today)
    }

    suspend fun upsertItems(clockInItems: List<ClockInItem>) {
        clockInItems.forEach {
            localRepo.upsertItem(it)
        }
    }

    suspend fun upsertRecords(clockInRecords: List<ClockInRecord>) {
        clockInRecords.forEach {
            localRepo.upsertRecord(it)
        }
    }

    suspend fun getUnsyncedItems(): List<ClockInItem> {
        return localRepo.getUnsyncedItems(getCurrentUser())
    }

    suspend fun getUnsyncedRecords(): List<ClockInRecord> {
        return localRepo.getUnnsyncedRecords(getCurrentUser())
    }
}