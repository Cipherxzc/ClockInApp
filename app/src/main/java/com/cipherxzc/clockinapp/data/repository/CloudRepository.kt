package com.cipherxzc.clockinapp.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

// Data Transfer Object for Firestore
data class ClockInItemDto(
    val itemId: String,
    val name: String,
    val description: String?,
    val clockInCount: Int,
    val lastModified: Timestamp,
    val isDeleted: Boolean
)

// 封装 Firestore 同步逻辑
class CloudRepository(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("clock_in_items")

    // 增量拉取自 lastSyncTime 之后的所有记录
    suspend fun fetchUpdatedSince(lastSync: Timestamp): List<ClockInItemDto> {
        val query = collection.whereGreaterThan("lastModified", lastSync)
        val snapshot: QuerySnapshot = query.get().await()
        return snapshot.documents.mapNotNull { it.toObject(ClockInItemDto::class.java) }
    }

    // 上传或覆盖多个条目
    suspend fun uploadItems(dtos: List<ClockInItemDto>) {
        dtos.forEach { dto ->
            val docRef = collection.document(dto.itemId)
            docRef.set(dto, SetOptions.merge()).await()
        }
    }

    // 标记远端逻辑删除
    suspend fun deleteItemRemotely(itemId: String) {
        val docRef = collection.document(itemId)
        docRef.update("isDeleted", true,
            "lastModified", Timestamp.now()).await()
    }
}
