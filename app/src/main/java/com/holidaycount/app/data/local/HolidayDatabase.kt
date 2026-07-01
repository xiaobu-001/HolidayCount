package com.holidaycount.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.holidaycount.app.data.local.dao.CustomEventDao
import com.holidaycount.app.data.local.dao.WidgetConfigDao
import com.holidaycount.app.data.local.entity.CustomEventEntity
import com.holidaycount.app.data.local.entity.WidgetConfigEntity

@Database(
    entities = [CustomEventEntity::class, WidgetConfigEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class HolidayDatabase : RoomDatabase() {
    abstract fun customEventDao(): CustomEventDao
    abstract fun widgetConfigDao(): WidgetConfigDao
}
