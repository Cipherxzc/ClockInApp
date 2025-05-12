package com.cipherxzc.clockinapp.data.repository

import com.cipherxzc.clockinapp.data.database.ClockInItem
import com.cipherxzc.clockinapp.data.database.ClockInRecord
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

// Data Transfer Object for Firestore
@IgnoreExtraProperties
data class ClockInItemDto(
    var name: String = "",
    var description: String? = null,
    var clockInCount: Int = 0,
    var lastModified: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    var isDeleted: Boolean = false
)

@IgnoreExtraProperties
data class ClockInRecordDto(
    var timestamp: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    var lastModified: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    var isDeleted: Boolean = false
)

// 封装 Firestore 同步逻辑
// layout: /users/{userId}/items/{itemId}/records/{recordId}
class CloudRepository(
    private val firestore: FirebaseFirestore
) {
    private fun ClockInItem.toDto(): ClockInItemDto = ClockInItemDto(
        name = name,
        description = description,
        clockInCount = clockInCount,
        lastModified = lastModified,
        isDeleted = isDeleted
    )

    private fun ClockInRecord.toDto(): ClockInRecordDto = ClockInRecordDto(
        timestamp = timestamp,
        lastModified = lastModified,
        isDeleted = isDeleted
    )

    private fun ClockInItemDto.toEntity(itemId: String, userId: String): ClockInItem = ClockInItem(
        itemId = itemId,
        userId = userId,
        name = name,
        description = description,
        clockInCount = clockInCount,
        lastModified = lastModified,
        isSynced = true,  // 已与云端同步
        isDeleted = isDeleted
    )

    private fun ClockInRecordDto.toEntity(recordId: String, userId: String, itemId: String): ClockInRecord = ClockInRecord(
        recordId = recordId,
        userId = userId,
        itemId = itemId,
        timestamp = timestamp,
        lastModified = lastModified,
        isSynced = true,  // 已与云端同步
        isDeleted = isDeleted
    )

    suspend fun pushItems(userId: String, items: List<ClockInItem>) {
        if (items.isEmpty()) return

        val batch = firestore.batch()
        val itemCollection = firestore.collection("users")
            .document(userId)
            .collection("items")
        items.forEach { item ->
            val itemDoc = itemCollection.document(item.itemId)
            batch.set(itemDoc, item.toDto(), SetOptions.merge())
        }
        batch.commit().await()
    }

    suspend fun pushRecords(userId: String, records: List<ClockInRecord>) {
        if (records.isEmpty()) return

        val batch = firestore.batch()
        val itemCollection = firestore.collection("users")
            .document(userId)
            .collection("items")
        records.forEach { record ->
            val recordDoc = itemCollection
                .document(record.itemId)
                .collection("records")
                .document(record.recordId)

            batch.set(recordDoc, record.toDto(), SetOptions.merge())
        }
        batch.commit().await()
    }

    suspend fun fetchUpdatedItems(userId: String, since: Timestamp): List<ClockInItem> {
        val snapshot = firestore
            .collection("users")
            .document(userId)
            .collection("items")
            .whereGreaterThan("lastModified", since)
            .get()
            .await()

        return snapshot.documents.map { doc ->
            val dto = doc.toObject(ClockInItemDto::class.java) ?: return@map null
            dto.toEntity(itemId = doc.id, userId = userId)
        }.filterNotNull()
    }

    suspend fun fetchUpdatedRecords(
        userId: String,
        since: Timestamp,
        updatedItems: List<ClockInItem>
    ): List<ClockInRecord> = coroutineScope {
        // 对每个更新过的 Item 并行拉取它下面所有更新过的 Record
        updatedItems.map { item ->
            async {
                val recordsSnapshot = firestore
                    .collection("users")
                    .document(userId)
                    .collection("items")
                    .document(item.itemId)
                    .collection("records")
                    .whereGreaterThan("lastModified", since)
                    .get()
                    .await()

                recordsSnapshot.documents.mapNotNull { doc ->
                    doc.toObject(ClockInRecordDto::class.java)?.toEntity(
                        recordId = doc.id,
                        userId = userId,
                        itemId = item.itemId
                    )
                }
            }
        }
    }.awaitAll().flatten()  // awaitAll 会返回 List<List<ClockInRecord>>，flatten 后变成 List<ClockInRecord>
}
