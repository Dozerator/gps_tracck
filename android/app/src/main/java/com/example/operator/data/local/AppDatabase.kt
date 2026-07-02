package com.example.operator.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.operator.data.local.dao.PendingPointDao
import com.example.operator.data.local.entity.PendingPointEntity

@Database(
    entities = [PendingPointEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pendingPointDao(): PendingPointDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "operator_local_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
