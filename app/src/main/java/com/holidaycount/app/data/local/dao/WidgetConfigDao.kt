package com.holidaycount.app.data.local.dao

import androidx.room.*
import com.holidaycount.app.data.local.entity.WidgetConfigEntity

@Dao
interface WidgetConfigDao {

    @Query("SELECT * FROM widget_configs WHERE widgetId = :widgetId")
    suspend fun getConfigByWidgetId(widgetId: Int): WidgetConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: WidgetConfigEntity)

    @Query("DELETE FROM widget_configs WHERE widgetId = :widgetId")
    suspend fun deleteByWidgetId(widgetId: Int)

    @Query("SELECT * FROM widget_configs")
    suspend fun getAllConfigs(): List<WidgetConfigEntity>
}
