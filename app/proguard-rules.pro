# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Room entities
-keep class com.holidaycount.app.data.local.entity.** { *; }
-keep class com.holidaycount.app.data.model.** { *; }

# Keep WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Keep Widget providers
-keep class * extends android.appwidget.AppWidgetProvider

# Keep BroadcastReceivers
-keep class com.holidaycount.app.notification.ReminderReceiver { *; }
-keep class com.holidaycount.app.widget.BootReceiver { *; }
