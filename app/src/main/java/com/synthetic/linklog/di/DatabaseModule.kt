package com.synthetic.linklog.di

import android.content.Context
import androidx.room.Room
import com.synthetic.linklog.data.local.LinkLogDatabase
import com.synthetic.linklog.data.local.dao.FolderDao
import com.synthetic.linklog.data.local.dao.LinkDao
import com.synthetic.linklog.data.local.dao.DownloadedVideoDao
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
    fun provideLinkLogDatabase(
        @ApplicationContext context: Context
    ): LinkLogDatabase {
        return Room.databaseBuilder(
            context,
            LinkLogDatabase::class.java,
            "link_log_db"
        )
        .fallbackToDestructiveMigration(true)
        .build()
    }

    @Provides
    @Singleton
    fun provideGroupDao(database: LinkLogDatabase): com.synthetic.linklog.data.local.dao.GroupDao {
        return database.groupDao()
    }

    @Provides
    @Singleton
    fun provideFolderDao(database: LinkLogDatabase): FolderDao {
        return database.folderDao()
    }

    @Provides
    @Singleton
    fun provideLinkDao(database: LinkLogDatabase): LinkDao {
        return database.linkDao()
    }

    @Provides
    @Singleton
    fun provideDownloadedVideoDao(database: LinkLogDatabase): DownloadedVideoDao {
        return database.downloadedVideoDao()
    }
}
