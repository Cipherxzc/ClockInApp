package com.cipherxzc.clockinapp.data.repository

import androidx.room.withTransaction
import com.cipherxzc.clockinapp.data.database.AppDatabase
import com.cipherxzc.clockinapp.data.database.ClockInItem
import com.cipherxzc.clockinapp.data.database.ClockInItemDao
import com.cipherxzc.clockinapp.data.database.ClockInRecord
import com.cipherxzc.clockinapp.data.database.ClockInRecordDao
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId

class LocalRepository(
    private val db: AppDatabase
) {
    private val itemDao: ClockInItemDao = db.clockInItemDao()
    private val recordDao: ClockInRecordDao = db.clockInRecordDao()

    private fun generateDocumentId(): String {
        return FirebaseFirestore.getInstance()
            .collection("anything")
            .document().id
    }

    private suspend fun insertOrUpdateItem(item: ClockInItem) {
        itemDao.insertOrUpdate(item)
    }

    suspend fun insertItem(userId: String, name: String, description: String?): ClockInItem {
        val item = ClockInItem(
            itemId = generateDocumentId(),
            userId = userId,
            name = name,
            description = description
        )
        insertOrUpdateItem(item)
        return item
    }

    suspend fun deleteItem(itemId: String) {
        val item = itemDao.getItemById(itemId)
        item?.let {
            itemDao.insertOrUpdate(it.modify(
                isDeleted = true
            ))
            recordDao.deleteRecordsByItem(item.userId, itemId)
        }
    }

    suspend fun incrementClockInCount(itemId: String) {
        val item = itemDao.getItemById(itemId)
        item?.let {
            itemDao.insertOrUpdate(it.modify(
                clockInCount = it.clockInCount + 1
            ))
        }
    }

    suspend fun decrementClockInCount(itemId: String) {
        val item = itemDao.getItemById(itemId)
        item?.let {
            itemDao.insertOrUpdate(it.modify(
                clockInCount = it.clockInCount - 1
            ))
        }
    }

    suspend fun getUnsyncedItems(userId: String): List<ClockInItem> =
        itemDao.getUnsyncedItems(userId)

    suspend fun upsertItems(items: List<ClockInItem>) {
        db.withTransaction {
            items.forEach {
                if (it.isDeleted) {
                    deleteItem(it.itemId)
                } else {
                    itemDao.insertOrUpdate(it.copy(
                        isSynced = true
                    ))
                }
            }
        }
    }

    suspend fun getItemById(itemId: String): ClockInItem? = itemDao.getItemById(itemId)
    suspend fun getItemsByUser(userId: String): List<ClockInItem> = itemDao.getItemsByUser(userId)

    suspend fun hasClockInOnDay(userId: String, itemId: String, day: LocalDate): Boolean {
        val zoneId = ZoneId.systemDefault()
        val startDateTimeMillis = day.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endDateTimeMillis = day.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
        return recordDao.hasRecordInRange(userId, itemId, startDateTimeMillis, endDateTimeMillis)
    }

    private suspend fun insertOrUpdateRecord(record: ClockInRecord) {
        recordDao.insertOrUpdate(record)
    }

    suspend fun insertRecord(userId: String, itemId: String): ClockInRecord {
        val record = ClockInRecord(
            recordId = generateDocumentId(),
            userId = userId,
            itemId = itemId
        )
        insertOrUpdateRecord(record)
        incrementClockInCount(itemId)
        return record
    }

    suspend fun deleteRecord(recordId: String) {
        val record = recordDao.getRecordById(recordId)
        record?.let {
            recordDao.insertOrUpdate(it.modify(
                isDeleted = true
            ))
            decrementClockInCount(it.itemId)
        }
    }

    suspend fun deleteMostRecentRecord(userId: String, itemId: String) {
        val record = recordDao.getMostRecentRecord(userId, itemId)
        record?.let {
            recordDao.insertOrUpdate(it.modify(
                isDeleted = true
            ))
            decrementClockInCount(it.itemId)
        }
    }

    suspend fun getUnnsyncedRecords(userId: String): List<ClockInRecord> =
        recordDao.getUnsyncedRecords(userId)

    suspend fun upsertRecords(records: List<ClockInRecord>) {
        db.withTransaction {
            records.forEach {
                if (it.isDeleted) {
                    deleteRecord(it.recordId)
                } else {
                    recordDao.insertOrUpdate(it.copy(
                        isSynced = true
                    ))
                }
            }
        }
    }

    suspend fun getRecordsByItem(userId: String, itemId: String): List<ClockInRecord> = recordDao.getRecordsByItem(userId, itemId)
    suspend fun getRecordById(recordId: String): ClockInRecord? = recordDao.getRecordById(recordId)

    suspend fun insertDefaultData(userId: String) = withContext(Dispatchers.IO) {
        val defaultItems = listOf(
            ClockInItem(
                itemId    = generateDocumentId(),
                userId    = userId,
                name      = "早起",
                clockInCount = 8,
                description  = "早睡早起身体好！"
            ),
            ClockInItem(
                itemId    = generateDocumentId(),
                userId    = userId,
                name      = "锻炼",
                clockInCount = 1,
                description  = "无体育，不华清！"
            ),
            ClockInItem(
                itemId    = generateDocumentId(),
                userId    = userId,
                name      = "读书",
                clockInCount = 3,
                description  = "书山有路勤为径！"
            ),
            ClockInItem(
                itemId    = generateDocumentId(),
                userId    = userId,
                name      = "背单词",
                clockInCount = 0,
                description  = "目标托福105分！"
            )
        )

        defaultItems.forEach { insertOrUpdateItem(it) }

        val defaultRecords = listOf(
            defaultItems[0].itemId to LocalDateTime.of(2025, Month.APRIL, 1, 9, 0),
            defaultItems[0].itemId to LocalDateTime.of(2025, Month.APRIL, 7, 9, 0),
            defaultItems[0].itemId to LocalDateTime.of(2025, Month.APRIL, 8, 9, 0),
            defaultItems[0].itemId to LocalDateTime.of(2025, Month.APRIL, 9, 9, 0),
            defaultItems[0].itemId to LocalDateTime.of(2025, Month.APRIL, 10, 9, 0),
            defaultItems[0].itemId to LocalDateTime.of(2025, Month.APRIL, 11, 9, 0),
            defaultItems[0].itemId to LocalDateTime.of(2025, Month.APRIL, 12, 9, 0),
            defaultItems[0].itemId to LocalDateTime.of(2025, Month.APRIL, 13, 9, 0),
            defaultItems[1].itemId to LocalDateTime.of(2025, Month.APRIL, 10, 9, 0),
            defaultItems[2].itemId to LocalDateTime.of(2025, Month.APRIL, 4, 9, 0),
            defaultItems[2].itemId to LocalDateTime.of(2025, Month.APRIL, 10, 9, 0),
            defaultItems[2].itemId to LocalDateTime.of(2025, Month.APRIL, 13, 9, 0)
        )

        val converter: (LocalDateTime) -> Timestamp = { localDateTime ->
            val zoneId = ZoneId.systemDefault()
            val instant = localDateTime.atZone(zoneId).toInstant()
            Timestamp(instant.epochSecond, instant.nano)
        }

        defaultRecords.forEach { (itemId, localDateTime) ->
            val record = ClockInRecord(
                recordId  = generateDocumentId(),
                userId    = userId,
                itemId    = itemId,
                timestamp = converter(localDateTime),
            )
            insertOrUpdateRecord(record)
        }
    }
}