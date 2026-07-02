package com.example.operator.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.operator.data.local.entity.PendingPointEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PendingPointDao_Impl implements PendingPointDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PendingPointEntity> __insertionAdapterOfPendingPointEntity;

  private final SharedSQLiteStatement __preparedStmtOfMarkAsSynced;

  private final SharedSQLiteStatement __preparedStmtOfMarkAttemptFailed;

  private final SharedSQLiteStatement __preparedStmtOfResetFailedToRetry;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSyncedOlderThan;

  public PendingPointDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPendingPointEntity = new EntityInsertionAdapter<PendingPointEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `pending_points` (`id`,`lat`,`lon`,`accuracy`,`userId`,`timestamp`,`objectType`,`direction`,`threatLevel`,`status`,`createdAt`,`syncAttempts`,`lastSyncAttempt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PendingPointEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindDouble(2, entity.getLat());
        statement.bindDouble(3, entity.getLon());
        statement.bindDouble(4, entity.getAccuracy());
        statement.bindString(5, entity.getUserId());
        statement.bindLong(6, entity.getTimestamp());
        statement.bindString(7, entity.getObjectType());
        statement.bindString(8, entity.getDirection());
        statement.bindString(9, entity.getThreatLevel());
        statement.bindString(10, entity.getStatus());
        statement.bindLong(11, entity.getCreatedAt());
        statement.bindLong(12, entity.getSyncAttempts());
        if (entity.getLastSyncAttempt() == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, entity.getLastSyncAttempt());
        }
      }
    };
    this.__preparedStmtOfMarkAsSynced = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE pending_points SET status = 'SYNCED' WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkAttemptFailed = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE pending_points\n"
                + "        SET syncAttempts = syncAttempts + 1,\n"
                + "            lastSyncAttempt = ?,\n"
                + "            status = CASE WHEN syncAttempts + 1 >= ? THEN 'FAILED' ELSE 'PENDING' END\n"
                + "        WHERE id = ?\n"
                + "        ";
        return _query;
      }
    };
    this.__preparedStmtOfResetFailedToRetry = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE pending_points SET status = 'PENDING', syncAttempts = 0 WHERE status = 'FAILED'";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteSyncedOlderThan = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM pending_points WHERE status = 'SYNCED' AND createdAt < ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final PendingPointEntity point,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPendingPointEntity.insertAndReturnId(point);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object markAsSynced(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAsSynced.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkAsSynced.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markAttemptFailed(final long id, final long time, final int maxAttempts,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAttemptFailed.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, time);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, maxAttempts);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkAttemptFailed.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object resetFailedToRetry(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfResetFailedToRetry.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfResetFailedToRetry.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSyncedOlderThan(final long olderThan,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSyncedOlderThan.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, olderThan);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteSyncedOlderThan.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getPendingPoints(final Continuation<? super List<PendingPointEntity>> $completion) {
    final String _sql = "SELECT * FROM pending_points WHERE status = 'PENDING' ORDER BY createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PendingPointEntity>>() {
      @Override
      @NonNull
      public List<PendingPointEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLat = CursorUtil.getColumnIndexOrThrow(_cursor, "lat");
          final int _cursorIndexOfLon = CursorUtil.getColumnIndexOrThrow(_cursor, "lon");
          final int _cursorIndexOfAccuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "accuracy");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfObjectType = CursorUtil.getColumnIndexOrThrow(_cursor, "objectType");
          final int _cursorIndexOfDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "direction");
          final int _cursorIndexOfThreatLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "threatLevel");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfSyncAttempts = CursorUtil.getColumnIndexOrThrow(_cursor, "syncAttempts");
          final int _cursorIndexOfLastSyncAttempt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncAttempt");
          final List<PendingPointEntity> _result = new ArrayList<PendingPointEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PendingPointEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpLat;
            _tmpLat = _cursor.getDouble(_cursorIndexOfLat);
            final double _tmpLon;
            _tmpLon = _cursor.getDouble(_cursorIndexOfLon);
            final float _tmpAccuracy;
            _tmpAccuracy = _cursor.getFloat(_cursorIndexOfAccuracy);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpObjectType;
            _tmpObjectType = _cursor.getString(_cursorIndexOfObjectType);
            final String _tmpDirection;
            _tmpDirection = _cursor.getString(_cursorIndexOfDirection);
            final String _tmpThreatLevel;
            _tmpThreatLevel = _cursor.getString(_cursorIndexOfThreatLevel);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final int _tmpSyncAttempts;
            _tmpSyncAttempts = _cursor.getInt(_cursorIndexOfSyncAttempts);
            final Long _tmpLastSyncAttempt;
            if (_cursor.isNull(_cursorIndexOfLastSyncAttempt)) {
              _tmpLastSyncAttempt = null;
            } else {
              _tmpLastSyncAttempt = _cursor.getLong(_cursorIndexOfLastSyncAttempt);
            }
            _item = new PendingPointEntity(_tmpId,_tmpLat,_tmpLon,_tmpAccuracy,_tmpUserId,_tmpTimestamp,_tmpObjectType,_tmpDirection,_tmpThreatLevel,_tmpStatus,_tmpCreatedAt,_tmpSyncAttempts,_tmpLastSyncAttempt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Integer> getPendingCount() {
    final String _sql = "SELECT COUNT(*) FROM pending_points WHERE status = 'PENDING'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"pending_points"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<PendingPointEntity>> getAllPoints() {
    final String _sql = "SELECT * FROM pending_points ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"pending_points"}, new Callable<List<PendingPointEntity>>() {
      @Override
      @NonNull
      public List<PendingPointEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLat = CursorUtil.getColumnIndexOrThrow(_cursor, "lat");
          final int _cursorIndexOfLon = CursorUtil.getColumnIndexOrThrow(_cursor, "lon");
          final int _cursorIndexOfAccuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "accuracy");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfObjectType = CursorUtil.getColumnIndexOrThrow(_cursor, "objectType");
          final int _cursorIndexOfDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "direction");
          final int _cursorIndexOfThreatLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "threatLevel");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfSyncAttempts = CursorUtil.getColumnIndexOrThrow(_cursor, "syncAttempts");
          final int _cursorIndexOfLastSyncAttempt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncAttempt");
          final List<PendingPointEntity> _result = new ArrayList<PendingPointEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PendingPointEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpLat;
            _tmpLat = _cursor.getDouble(_cursorIndexOfLat);
            final double _tmpLon;
            _tmpLon = _cursor.getDouble(_cursorIndexOfLon);
            final float _tmpAccuracy;
            _tmpAccuracy = _cursor.getFloat(_cursorIndexOfAccuracy);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpObjectType;
            _tmpObjectType = _cursor.getString(_cursorIndexOfObjectType);
            final String _tmpDirection;
            _tmpDirection = _cursor.getString(_cursorIndexOfDirection);
            final String _tmpThreatLevel;
            _tmpThreatLevel = _cursor.getString(_cursorIndexOfThreatLevel);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final int _tmpSyncAttempts;
            _tmpSyncAttempts = _cursor.getInt(_cursorIndexOfSyncAttempts);
            final Long _tmpLastSyncAttempt;
            if (_cursor.isNull(_cursorIndexOfLastSyncAttempt)) {
              _tmpLastSyncAttempt = null;
            } else {
              _tmpLastSyncAttempt = _cursor.getLong(_cursorIndexOfLastSyncAttempt);
            }
            _item = new PendingPointEntity(_tmpId,_tmpLat,_tmpLon,_tmpAccuracy,_tmpUserId,_tmpTimestamp,_tmpObjectType,_tmpDirection,_tmpThreatLevel,_tmpStatus,_tmpCreatedAt,_tmpSyncAttempts,_tmpLastSyncAttempt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
