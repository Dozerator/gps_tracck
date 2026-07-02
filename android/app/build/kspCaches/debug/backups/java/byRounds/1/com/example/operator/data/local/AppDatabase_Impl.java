package com.example.operator.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.example.operator.data.local.dao.PendingPointDao;
import com.example.operator.data.local.dao.PendingPointDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile PendingPointDao _pendingPointDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `pending_points` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `lat` REAL NOT NULL, `lon` REAL NOT NULL, `accuracy` REAL NOT NULL, `userId` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `objectType` TEXT NOT NULL, `direction` TEXT NOT NULL, `threatLevel` TEXT NOT NULL, `status` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `syncAttempts` INTEGER NOT NULL, `lastSyncAttempt` INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'e35b9d43ec7a9796de2b482e0f39a913')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `pending_points`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsPendingPoints = new HashMap<String, TableInfo.Column>(13);
        _columnsPendingPoints.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingPoints.put("lat", new TableInfo.Column("lat", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingPoints.put("lon", new TableInfo.Column("lon", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingPoints.put("accuracy", new TableInfo.Column("accuracy", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingPoints.put("userId", new TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingPoints.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingPoints.put("objectType", new TableInfo.Column("objectType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingPoints.put("direction", new TableInfo.Column("direction", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingPoints.put("threatLevel", new TableInfo.Column("threatLevel", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingPoints.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingPoints.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingPoints.put("syncAttempts", new TableInfo.Column("syncAttempts", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingPoints.put("lastSyncAttempt", new TableInfo.Column("lastSyncAttempt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPendingPoints = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPendingPoints = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPendingPoints = new TableInfo("pending_points", _columnsPendingPoints, _foreignKeysPendingPoints, _indicesPendingPoints);
        final TableInfo _existingPendingPoints = TableInfo.read(db, "pending_points");
        if (!_infoPendingPoints.equals(_existingPendingPoints)) {
          return new RoomOpenHelper.ValidationResult(false, "pending_points(com.example.operator.data.local.entity.PendingPointEntity).\n"
                  + " Expected:\n" + _infoPendingPoints + "\n"
                  + " Found:\n" + _existingPendingPoints);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "e35b9d43ec7a9796de2b482e0f39a913", "16a068ea1a9ecc1935c83586f920dea9");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "pending_points");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `pending_points`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(PendingPointDao.class, PendingPointDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public PendingPointDao pendingPointDao() {
    if (_pendingPointDao != null) {
      return _pendingPointDao;
    } else {
      synchronized(this) {
        if(_pendingPointDao == null) {
          _pendingPointDao = new PendingPointDao_Impl(this);
        }
        return _pendingPointDao;
      }
    }
  }
}
