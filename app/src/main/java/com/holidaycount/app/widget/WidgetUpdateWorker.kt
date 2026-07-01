package com.holidaycount.app.widget

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * WorkManager 定时更新任务
 * 每天凌晨 00:05 刷新所有小部件
 */
@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted private val ctx: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        return try {
            HolidayWidgetProvider4x1.updateAllWidgets(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "holiday_widget_daily_update"

        /**
         * 调度每日更新任务
         * 使用 PeriodicWorkRequest，每24小时执行一次
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .build()

            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        /**
         * 计算到明天 00:05 的毫秒数
         */
        private fun calculateInitialDelay(): Long {
            val now = System.currentTimeMillis()
            val tomorrow = com.holidaycount.app.utils.DateCalculation.tomorrowStartMs()
            val targetTime = tomorrow + 5 * 60 * 1000L // 明天 00:05
            return (targetTime - now).coerceAtLeast(0)
        }

        /**
         * 取消调度
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
