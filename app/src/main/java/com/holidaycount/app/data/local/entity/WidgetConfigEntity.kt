package com.holidaycount.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 小部件配置数据库实体
 */
@Entity(tableName = "widget_configs")
data class WidgetConfigEntity(
    @PrimaryKey
    val widgetId: Int,

    /** 关联的事件 ID，-1 表示显示最近事件 */
    val eventId: Long = -1L,

    /** 是否显示最近事件（忽略 eventId）*/
    val showNextEvent: Boolean = true,

    /** 主题颜色 */
    val accentColor: Int = 0xFF6200EE.toInt(),

    /** 字体大小因子（1.0f = 正常）*/
    val fontSizeFactor: Float = 1.0f,

    /** 是否显示日期 */
    val showDate: Boolean = true,

    /** 是否显示 emoji */
    val showEmoji: Boolean = true,

    /** 背景透明度（0~255）*/
    val backgroundAlpha: Int = 210,

    val updatedAt: Long = System.currentTimeMillis()
)
