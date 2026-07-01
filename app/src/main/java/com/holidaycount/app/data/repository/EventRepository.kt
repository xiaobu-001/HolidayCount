package com.holidaycount.app.data.repository

import com.holidaycount.app.data.local.dao.CustomEventDao
import com.holidaycount.app.data.local.dao.WidgetConfigDao
import com.holidaycount.app.data.local.entity.CustomEventEntity
import com.holidaycount.app.data.local.entity.WidgetConfigEntity
import com.holidaycount.app.data.model.EventItem
import com.holidaycount.app.data.model.EventType
import com.holidaycount.app.utils.DateCalculation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val customEventDao: CustomEventDao,
    private val widgetConfigDao: WidgetConfigDao
) {

    /** 监听自定义事件流（包含内置节假日合并） */
    fun getAllEventsFlow(): Flow<List<EventItem>> {
        return customEventDao.getAllEventsFlow().map { entities ->
            val customItems = entities.map { it.toEventItem() }
            val builtInItems = BuiltInHolidays.getAllHolidays()
            (customItems + builtInItems).sortedBy { item ->
                val d = item.daysLeft
                if (d < 0) 100000 - d else d
            }
        }
    }

    /** 获取所有自定义事件（不含内置）*/
    fun getCustomEventsFlow(): Flow<List<EventItem>> {
        return customEventDao.getAllEventsFlow().map { entities ->
            entities.map { it.toEventItem() }
        }
    }

    /** 获取下一个最近的节假日/事件 */
    suspend fun getNextEvent(): EventItem? {
        val customItems = customEventDao.getAllEvents().map { it.toEventItem() }
        val builtInItems = BuiltInHolidays.getAllHolidays()
        val all = (customItems + builtInItems)
        return all.filter { it.daysLeft >= 0 }.minByOrNull { it.daysLeft }
    }

    /** 根据 Widget 配置获取对应事件 */
    suspend fun getEventForWidget(widgetId: Int): EventItem? {
        val config = widgetConfigDao.getConfigByWidgetId(widgetId) ?: return getNextEvent()
        return if (config.showNextEvent || config.eventId < 0) {
            getNextEvent()
        } else {
            val entity = customEventDao.getEventById(config.eventId) ?: return getNextEvent()
            entity.toEventItem()
        }
    }

    suspend fun insertEvent(event: CustomEventEntity): Long = customEventDao.insertEvent(event)

    suspend fun updateEvent(event: CustomEventEntity) = customEventDao.updateEvent(event)

    suspend fun deleteEvent(event: CustomEventEntity) = customEventDao.deleteEvent(event)

    suspend fun deleteEventById(id: Long) = customEventDao.deleteEventById(id)

    suspend fun getEventById(id: Long): CustomEventEntity? = customEventDao.getEventById(id)

    suspend fun saveWidgetConfig(config: WidgetConfigEntity) = widgetConfigDao.insertOrUpdate(config)

    suspend fun getWidgetConfig(widgetId: Int): WidgetConfigEntity? = widgetConfigDao.getConfigByWidgetId(widgetId)

    suspend fun deleteWidgetConfig(widgetId: Int) = widgetConfigDao.deleteByWidgetId(widgetId)

    /** 将 Entity 转为 EventItem（计算剩余天数）*/
    private fun CustomEventEntity.toEventItem(): EventItem {
        val effectiveTargetMs = DateCalculation.getNextOccurrenceByRule(targetDateMs, repeatRule)
        val daysLeft = DateCalculation.daysUntil(effectiveTargetMs)
        return EventItem(
            id = id,
            name = name,
            emoji = emoji,
            targetDate = effectiveTargetMs,
            daysLeft = daysLeft,
            eventType = eventType,
            repeatRule = repeatRule,
            backgroundColor = backgroundColor,
            isEnabled = isEnabled,
            notifyDays = getNotifyDaysList()
        )
    }
}
