package com.holidaycount.app.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.holidaycount.app.R
import com.holidaycount.app.ui.main.MainActivity
import java.util.Calendar

/**
 * 通知管理器
 */
object NotificationHelper {

    const val CHANNEL_ID = "holiday_countdown_channel"
    const val CHANNEL_NAME = "节假日提醒"
    const val NOTIFICATION_ID_BASE = 10000

    /**
     * 创建通知渠道（Android 8+ 必须）
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "节假日倒计时提醒通知"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * 发送倒计时提醒通知
     */
    fun sendReminderNotification(
        context: Context,
        eventId: Long,
        eventName: String,
        emoji: String,
        daysLeft: Int
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openIntent = Intent(context, MainActivity::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(context, eventId.toInt(), openIntent, flags)

        val title = when (daysLeft) {
            0 -> "$emoji $eventName 就是今天！"
            1 -> "$emoji $eventName 明天就到！"
            else -> "$emoji 距离 $eventName 还有 $daysLeft 天"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText("点击查看详情")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify((NOTIFICATION_ID_BASE + eventId).toInt(), notification)
    }

    /**
     * 设置精确提醒闹钟
     */
    fun scheduleReminder(
        context: Context,
        eventId: Long,
        eventName: String,
        emoji: String,
        targetDateMs: Long,
        daysBeforeList: List<Int>,
        hour: Int,
        minute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        daysBeforeList.forEach { daysBefore ->
            val triggerMs = targetDateMs - (daysBefore.toLong() * 86400000L)
            val cal = Calendar.getInstance()
            cal.timeInMillis = triggerMs
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            if (cal.timeInMillis > System.currentTimeMillis()) {
                val intent = Intent(context, ReminderReceiver::class.java).apply {
                    putExtra("event_id", eventId)
                    putExtra("event_name", eventName)
                    putExtra("emoji", emoji)
                    putExtra("days_left", daysBefore)
                }
                val requestCode = (eventId * 100 + daysBefore).toInt()
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
                val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent
                        )
                    } else {
                        alarmManager.set(
                            AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent
                        )
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent
                    )
                }
            }
        }
    }

    /**
     * 取消事件的所有提醒
     */
    fun cancelReminders(context: Context, eventId: Long, daysBeforeList: List<Int>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        daysBeforeList.forEach { daysBefore ->
            val intent = Intent(context, ReminderReceiver::class.java)
            val requestCode = (eventId * 100 + daysBefore).toInt()
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_NO_CREATE
            }
            val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }
}

/**
 * 提醒广播接收器
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getLongExtra("event_id", -1)
        val eventName = intent.getStringExtra("event_name") ?: return
        val emoji = intent.getStringExtra("emoji") ?: "🎉"
        val daysLeft = intent.getIntExtra("days_left", 0)

        NotificationHelper.sendReminderNotification(context, eventId, eventName, emoji, daysLeft)
    }
}
