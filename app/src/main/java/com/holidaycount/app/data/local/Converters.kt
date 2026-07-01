package com.holidaycount.app.data.local

import androidx.room.TypeConverter
import com.holidaycount.app.data.model.DateType
import com.holidaycount.app.data.model.EventType
import com.holidaycount.app.data.model.RepeatRule

class Converters {

    @TypeConverter
    fun fromEventType(value: EventType): String = value.name

    @TypeConverter
    fun toEventType(value: String): EventType = EventType.valueOf(value)

    @TypeConverter
    fun fromRepeatRule(value: RepeatRule): String = value.name

    @TypeConverter
    fun toRepeatRule(value: String): RepeatRule = RepeatRule.valueOf(value)

    @TypeConverter
    fun fromDateType(value: DateType): String = value.name

    @TypeConverter
    fun toDateType(value: String): DateType = DateType.valueOf(value)
}
