package com.holidaycount.app.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.holidaycount.app.databinding.ActivityMainBinding
import com.holidaycount.app.notification.NotificationHelper
import com.holidaycount.app.widget.WidgetUpdateWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // 处理通知权限结果
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViewPager()
        setupFab()
        setupNextEventCard()
        requestNotificationPermission()

        // 初始化通知渠道
        NotificationHelper.createNotificationChannel(this)

        // 启动 WorkManager 定时任务
        WidgetUpdateWorker.schedule(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "HolidayCount"
    }

    private fun setupViewPager() {
        val adapter = EventPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "即将到来"
                1 -> "全部事件"
                2 -> "已过期"
                else -> ""
            }
        }.attach()
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val dialog = com.holidaycount.app.ui.add.AddEventBottomSheet()
            dialog.show(supportFragmentManager, "AddEventBottomSheet")
        }
    }

    private fun setupNextEventCard() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.nextEvent.collect { event ->
                    if (event != null) {
                        binding.cardNextEvent.visibility = View.VISIBLE
                        binding.tvNextEventEmoji.text = event.emoji
                        binding.tvNextEventName.text = event.name
                        binding.tvNextEventDays.text = if (event.daysLeft == 0) "今天！" else "${event.daysLeft}"
                        binding.tvNextEventDaysUnit.text = if (event.daysLeft == 0) "" else "天"
                        binding.tvNextEventDate.text = com.holidaycount.app.utils.DateCalculation
                            .formatDate(event.targetDate)
                    } else {
                        binding.cardNextEvent.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
