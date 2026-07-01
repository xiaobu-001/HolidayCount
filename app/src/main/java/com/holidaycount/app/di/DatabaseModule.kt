package com.holidaycount.app.di

import android.content.Context
import androidx.room.Room
import com.holidaycount.app.data.local.HolidayDatabase
import com.holidaycount.app.data.local.dao.CustomEventDao
import com.holidaycount.app.data.local.dao.WidgetConfigDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HolidayDatabase {
        return Room.databaseBuilder(
            context,
            HolidayDatabase::class.java,
            "holiday_count.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideCustomEventDao(db: HolidayDatabase): CustomEventDao = db.customEventDao()

    @Provides
    fun provideWidgetConfigDao(db: HolidayDatabase): WidgetConfigDao = db.widgetConfigDao()
}
