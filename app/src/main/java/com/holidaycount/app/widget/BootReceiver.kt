package com.holidaycount.app.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 开机广播接收器
 * 手机重启后重新注册 WorkManager 任务
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            // 重新调度每日更新
            WidgetUpdateWorker.schedule(context)
            // 立即刷新一次
            HolidayWidgetProvider4x1.updateAllWidgets(context)
        }
    }
}
