# HolidayCount - 节假日倒计时桌面小部件 App

一款 Android 桌面小部件应用，实时显示距离下一个重要节假日的剩余天数。

## 功能特性

- 🗓️ **三种小部件尺寸**：2×1、4×1、4×2，满足不同桌面布局需求
- 🧧 **内置 25+ 节假日**：春节、元宵、清明、端午、七夕、中秋、国庆、圣诞等
- 🌙 **农历自动计算**：农历节日自动转换公历日期，每年自动更新
- ✏️ **自定义事件**：添加生日、纪念日等个人重要日期，支持每年/每月重复
- 🔔 **提前提醒**：支持提前 1/3/7/15 天推送通知提醒
- 🎨 **Material You 主题**：支持 Android 12+ 动态取色，深色模式自适应
- ⚡ **每日自动刷新**：WorkManager 保证每天凌晨更新倒计时天数

## 项目结构

```
app/src/main/java/com/holidaycount/app/
├── HolidayCountApp.kt          # Application + Hilt + WorkManager
├── data/
│   ├── local/
│   │   ├── HolidayDatabase.kt  # Room 数据库
│   │   ├── Converters.kt       # 类型转换器
│   │   ├── dao/
│   │   │   ├── CustomEventDao.kt
│   │   │   └── WidgetConfigDao.kt
│   │   └── entity/
│   │       ├── CustomEventEntity.kt
│   │       └── WidgetConfigEntity.kt
│   ├── model/
│   │   └── EventItem.kt        # UI 数据模型
│   └── repository/
│       ├── EventRepository.kt  # 数据仓库
│       └── BuiltInHolidays.kt  # 内置节假日数据
├── widget/
│   ├── HolidayWidgetProvider.kt  # 三种尺寸 Widget Provider
│   ├── WidgetUpdateWorker.kt     # WorkManager 定时更新
│   └── BootReceiver.kt           # 开机恢复定时任务
├── ui/
│   ├── main/
│   │   ├── MainActivity.kt
│   │   ├── MainViewModel.kt
│   │   ├── EventListFragment.kt
│   │   ├── EventListAdapter.kt
│   │   └── EventPagerAdapter.kt
│   ├── add/
│   │   ├── AddEventBottomSheet.kt
│   │   └── AddEventViewModel.kt
│   └── widgetconfig/
│       └── WidgetConfigActivity.kt
├── notification/
│   └── NotificationHelper.kt   # 通知 + 定时闹钟
├── utils/
│   ├── LunarCalendar.kt        # 农历计算（1900-2049年）
│   └── DateCalculation.kt      # 日期差值计算
└── di/
    └── DatabaseModule.kt       # Hilt DI 配置
```

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin |
| 架构 | MVVM + Repository |
| UI | Material Design 3 |
| 数据库 | Room |
| 依赖注入 | Hilt |
| 异步 | Kotlin Coroutines + Flow |
| 后台任务 | WorkManager |
| 最低 SDK | API 24 (Android 7.0) |
| 目标 SDK | API 34 (Android 14) |

## 快速开始

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高
- JDK 17
- Android SDK 34

### 构建步骤

1. 用 Android Studio 打开项目根目录
2. 等待 Gradle 同步完成
3. 连接 Android 设备或启动模拟器（API 24+）
4. 点击 Run 按钮运行

### 添加桌面小部件

1. 安装应用后，长按桌面空白处
2. 选择「小部件」→「HolidayCount」
3. 选择 2×1、4×1 或 4×2 尺寸
4. 配置显示内容后点击「添加小部件」

## 内置节假日列表

### 中国法定节假日（公历）
元旦(1/1)、劳动节(5/1)、国庆节(10/1)

### 中国传统节日（农历自动计算）
春节🧧、元宵节🏮、清明节🌿、端午节🐉、七夕节💑、
中秋节🌕、重阳节🍂、冬至❄️、腊八节🥣、除夕🎉

### 国际节日（公历）
情人节💝(2/14)、妇女节💐(3/8)、愚人节🃏(4/1)、
儿童节🧸(6/1)、万圣节🎃(10/31)、感恩节🦃(11月第4周四)、
平安夜🌟(12/24)、圣诞节🎄(12/25)

## 待完善项目

- [ ] `mipmap-*/ic_launcher.png` — 需要添加实际应用图标
- [ ] Gradle Wrapper JAR — 需要运行 `gradle wrapper` 生成
- [ ] 数据库迁移脚本（未来版本升级时）
- [ ] 网络接口（可选：动态更新节假日数据）
- [ ] 小部件堆叠支持（Android 12+）
- [ ] 更多农历节气（立春、夏至等）

## 运行单元测试

```bash
./gradlew test
```

测试覆盖：
- `DateCalculationTest` - 日期计算逻辑
- `LunarCalendarTest` - 农历转换算法（含春节日期验证）
