package com.example.operator.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.operator.data.local.dao.PendingPointDao
import com.example.operator.data.local.entity.PendingPointEntity

@Database(
    entities = [PendingPointEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pendingPointDao(): PendingPointDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // v2 → v3: добавлено trackId (группировка точек одного объекта в трек — см.
        // utils/TrackManager.kt). Таблица теперь служит и локальной историей за смену
        // (не только очередью на отправку), поэтому в этот раз пишем настоящую миграцию
        // вместо fallbackToDestructiveMigration — иначе апдейт приложения посреди смены
        // стирал бы всю накопленную историю.
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE pending_points ADD COLUMN trackId TEXT DEFAULT NULL")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "operator_local_db"
                )
                    .addMigrations(MIGRATION_2_3)
                    // Подстраховка на случай будущих схем без явной миграции.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
