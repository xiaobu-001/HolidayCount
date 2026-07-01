package com.holidaycount.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.holidaycount.app.R
import com.holidaycount.app.data.local.HolidayDatabase
import com.holidaycount.app.data.repository.EventRepository
import com.holidaycount.app.ui.main.MainActivity
import com.holidaycount.app.utils.DateCalculation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.room.Room

/**
 * 4×1 桌面小部件 Provider
 */
class HolidayWidgetProvider4x1 : AppWidgetProvider() {

    companion object {
        const val ACTION_CLICK_REFRESH = "com.holidaycount.app.WIDGET_CLICK_REFRESH"

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component4x1 = ComponentName(context, HolidayWidgetProvider4x1::class.java)
            val component2x1 = ComponentName(context, HolidayWidgetProvider2x1::class.java)
            val component4x2 = ComponentName(context, HolidayWidgetProvider4x2::class.java)

            manager.getAppWidgetIds(component4x1).forEach { id ->
                WidgetUpdater.updateWidget4x1(context, manager, id)
            }
            manager.getAppWidgetIds(component2x1).forEach { id ->
                WidgetUpdater.updateWidget2x1(context, manager, id)
            }
            manager.getAppWidgetIds(component4x2).forEach { id ->
                WidgetUpdater.updateWidget4x2(context, manager, id)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            WidgetUpdater.updateWidget4x1(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_CLICK_REFRESH) {
            val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)
            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                WidgetUpdater.updateWidget4x1(context, AppWidgetManager.getInstance(context), widgetId)
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = Room.databaseBuilder(context, HolidayDatabase::class.java, "holiday_count.db").build()
            appWidgetIds.forEach { db.widgetConfigDao().deleteByWidgetId(it) }
        }
    }
}

/**
 * 2×1 小部件 Provider
 */
class HolidayWidgetProvider2x1 : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            WidgetUpdater.updateWidget2x1(context, appWidgetManager, widgetId)
        }
    }
}

/**
 * 4×2 小部件 Provider
 */
class HolidayWidgetProvider4x2 : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            WidgetUpdater.updateWidget4x2(context, appWidgetManager, widgetId)
        }
    }
}

/**
 * Widget 更新工具（统一处理 RemoteViews 更新逻辑）
 */
object WidgetUpdater {

    fun updateWidget4x1(context: Context, manager: AppWidgetManager, widgetId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val event = getNextEvent(context, widgetId)
            val views = RemoteViews(context.packageName, R.layout.widget_4x1)

            if (event != null) {
                views.setTextViewText(R.id.tv_widget_emoji, event.emoji)
                views.setTextViewText(R.id.tv_widget_name, event.name)
                views.setTextViewText(R.id.tv_widget_days,
                    if (event.daysLeft == 0) "今天！" else "${event.daysLeft}")
                views.setTextViewText(R.id.tv_widget_days_unit,
                    if (event.daysLeft == 0) "" else "天")
                views.setTextViewText(R.id.tv_widget_date,
                    DateCalculation.formatDate(event.targetDate) + " " +
                    DateCalculation.getWeekday(event.targetDate))
            } else {
                views.setTextViewText(R.id.tv_widget_name, "暂无事件")
                views.setTextViewText(R.id.tv_widget_days, "--")
                views.setTextViewText(R.id.tv_widget_days_unit, "天")
                views.setTextViewText(R.id.tv_widget_date, "点击添加事件")
            }

            // 点击打开 App
            views.setOnClickPendingIntent(R.id.widget_container, getMainPendingIntent(context))
            // 刷新按钮
            views.setOnClickPendingIntent(R.id.btn_widget_refresh, getRefreshPendingIntent(context, widgetId))

            manager.updateAppWidget(widgetId, views)
        }
    }

    fun updateWidget2x1(context: Context, manager: AppWidgetManager, widgetId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val event = getNextEvent(context, widgetId)
            val views = RemoteViews(context.packageName, R.layout.widget_2x1)

            if (event != null) {
                views.setTextViewText(R.id.tv_widget_emoji, event.emoji)
                views.setTextViewText(R.id.tv_widget_name, event.name)
                views.setTextViewText(R.id.tv_widget_days,
                    if (event.daysLeft == 0) "今天" else "${event.daysLeft}天")
            } else {
                views.setTextViewText(R.id.tv_widget_name, "暂无事件")
                views.setTextViewText(R.id.tv_widget_days, "--")
            }

            views.setOnClickPendingIntent(R.id.widget_container, getMainPendingIntent(context))
            manager.updateAppWidget(widgetId, views)
        }
    }

    fun updateWidget4x2(context: Context, manager: AppWidgetManager, widgetId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val event = getNextEvent(context, widgetId)
            val views = RemoteViews(context.packageName, R.layout.widget_4x2)

            if (event != null) {
                views.setTextViewText(R.id.tv_widget_emoji, event.emoji)
                views.setTextViewText(R.id.tv_widget_name, event.name)
                views.setTextViewText(R.id.tv_widget_days,
                    if (event.daysLeft == 0) "今天！" else "${event.daysLeft}")
                views.setTextViewText(R.id.tv_widget_days_label,
                    if (event.daysLeft == 0) "就是今天" else "天后到来")
                views.setTextViewText(R.id.tv_widget_date,
                    DateCalculation.formatDate(event.targetDate))
                views.setTextViewText(R.id.tv_widget_weekday,
                    DateCalculation.getWeekday(event.targetDate))
                views.setTextViewText(R.id.tv_widget_countdown_hint,
                    DateCalculation.formatDaysLeft(event.daysLeft))
            } else {
                views.setTextViewText(R.id.tv_widget_name, "点击添加倒计时")
                views.setTextViewText(R.id.tv_widget_days, "?")
                views.setTextViewText(R.id.tv_widget_days_label, "天")
                views.setTextViewText(R.id.tv_widget_date, "")
                views.setTextViewText(R.id.tv_widget_weekday, "")
                views.setTextViewText(R.id.tv_widget_countdown_hint, "")
            }

            views.setOnClickPendingIntent(R.id.widget_container, getMainPendingIntent(context))
            views.setOnClickPendingIntent(R.id.btn_widget_refresh, getRefreshPendingIntent(context, widgetId))
            manager.updateAppWidget(widgetId, views)
        }
    }

    private suspend fun getNextEvent(context: Context, widgetId: Int): com.holidaycount.app.data.model.EventItem? {
        return try {
            val db = Room.databaseBuilder(context, HolidayDatabase::class.java, "holiday_count.db").build()
            val repo = EventRepository(db.customEventDao(), db.widgetConfigDao())
            repo.getEventForWidget(widgetId)
        } catch (e: Exception) {
            null
        }
    }

    private fun getMainPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(context, 0, intent, flags)
    }

    private fun getRefreshPendingIntent(context: Context, widgetId: Int): PendingIntent {
        val intent = Intent(context, HolidayWidgetProvider4x1::class.java).apply {
            action = HolidayWidgetProvider4x1.ACTION_CLICK_REFRESH
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context, widgetId, intent, flags)
    }
}
