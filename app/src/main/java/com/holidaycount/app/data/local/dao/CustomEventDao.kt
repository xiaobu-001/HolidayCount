package com.holidaycount.app.data.local.dao

import androidx.room.*
import com.holidaycount.app.data.local.entity.CustomEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomEventDao {

    @Query("SELECT * FROM custom_events ORDER BY targetDateMs ASC")
    fun getAllEventsFlow(): Flow<List<CustomEventEntity>>

    @Query("SELECT * FROM custom_events WHERE isEnabled = 1 ORDER BY targetDateMs ASC")
    fun getEnabledEventsFlow(): Flow<List<CustomEventEntity>>

    @Query("SELECT * FROM custom_events ORDER BY targetDateMs ASC")
    suspend fun getAllEvents(): List<CustomEventEntity>

    @Query("SELECT * FROM custom_events WHERE id = :id")
    suspend fun getEventById(id: Long): CustomEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CustomEventEntity): Long

    @Update
    suspend fun updateEvent(event: CustomEventEntity)

    @Delete
    suspend fun deleteEvent(event: CustomEventEntity)

    @Query("DELETE FROM custom_events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Query("SELECT COUNT(*) FROM custom_events")
    suspend fun getEventCount(): Int
}
