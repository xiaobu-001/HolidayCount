package com.holidaycount.app.ui.widgetconfig

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.holidaycount.app.data.local.entity.WidgetConfigEntity
import com.holidaycount.app.data.repository.EventRepository
import com.holidaycount.app.databinding.ActivityWidgetConfigBinding
import com.holidaycount.app.widget.HolidayWidgetProvider4x1
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WidgetConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWidgetConfigBinding
    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    @Inject
    lateinit var repository: EventRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWidgetConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 默认结果为取消
        setResult(Activity.RESULT_CANCELED)

        widgetId = intent.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setupViews()
    }

    private fun setupViews() {
        binding.radioShowNext.isChecked = true // 默认显示最近事件

        binding.btnConfirm.setOnClickListener {
            saveConfig()
        }

        binding.btnCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun saveConfig() {
        lifecycleScope.launch {
            try {
                val config = WidgetConfigEntity(
                    widgetId = widgetId,
                    showNextEvent = binding.radioShowNext.isChecked,
                    showDate = binding.checkShowDate.isChecked,
                    showEmoji = binding.checkShowEmoji.isChecked
                )
                repository.saveWidgetConfig(config)

                // 更新 Widget
                HolidayWidgetProvider4x1.updateAllWidgets(this@WidgetConfigActivity)

                val resultValue = Intent().apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                }
                setResult(Activity.RESULT_OK, resultValue)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@WidgetConfigActivity, "配置保存失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
