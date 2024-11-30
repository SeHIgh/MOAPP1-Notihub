package com.example.notihub.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class UserPreferenceDao_Impl implements UserPreferenceDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<UserPreferenceEntity> __insertionAdapterOfUserPreferenceEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeletePreferenceByKeyword;

  private final SharedSQLiteStatement __preparedStmtOfResetAllPreferences;

  private final SharedSQLiteStatement __preparedStmtOfUpdateWeightByKeyword;

  public UserPreferenceDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUserPreferenceEntity = new EntityInsertionAdapter<UserPreferenceEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `user_preference` (`keyword`,`weight`,`feedback`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserPreferenceEntity entity) {
        statement.bindString(1, entity.getKeyword());
        statement.bindDouble(2, entity.getWeight());
        statement.bindLong(3, entity.getFeedback());
      }
    };
    this.__preparedStmtOfDeletePreferenceByKeyword = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM user_preference WHERE keyword = ?";
        return _query;
      }
    };
    this.__preparedStmtOfResetAllPreferences = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE user_preference SET weight = 0.0";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateWeightByKeyword = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE user_preference SET weight = ? WHERE keyword = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertOrUpdatePreference(final UserPreferenceEntity preference,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUserPreferenceEntity.insert(preference);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePreferenceByKeyword(final String keyword,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeletePreferenceByKeyword.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, keyword);
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
          __preparedStmtOfDeletePreferenceByKeyword.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object resetAllPreferences(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfResetAllPreferences.acquire();
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
          __preparedStmtOfResetAllPreferences.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateWeightByKeyword(final String keyword, final double newWeight,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateWeightByKeyword.acquire();
        int _argIndex = 1;
        _stmt.bindDouble(_argIndex, newWeight);
        _argIndex = 2;
        _stmt.bindString(_argIndex, keyword);
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
          __preparedStmtOfUpdateWeightByKeyword.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getPreferenceByKeyword(final String keyword,
      final Continuation<? super UserPreferenceEntity> $completion) {
    final String _sql = "SELECT * FROM user_preference WHERE keyword = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, keyword);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<UserPreferenceEntity>() {
      @Override
      @Nullable
      public UserPreferenceEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfKeyword = CursorUtil.getColumnIndexOrThrow(_cursor, "keyword");
          final int _cursorIndexOfWeight = CursorUtil.getColumnIndexOrThrow(_cursor, "weight");
          final int _cursorIndexOfFeedback = CursorUtil.getColumnIndexOrThrow(_cursor, "feedback");
          final UserPreferenceEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpKeyword;
            _tmpKeyword = _cursor.getString(_cursorIndexOfKeyword);
            final double _tmpWeight;
            _tmpWeight = _cursor.getDouble(_cursorIndexOfWeight);
            final int _tmpFeedback;
            _tmpFeedback = _cursor.getInt(_cursorIndexOfFeedback);
            _result = new UserPreferenceEntity(_tmpKeyword,_tmpWeight,_tmpFeedback);
          } else {
            _result = null;
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
  public Object getAllPreferences(
      final Continuation<? super List<UserPreferenceEntity>> $completion) {
    final String _sql = "SELECT * FROM user_preference ORDER BY weight DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<UserPreferenceEntity>>() {
      @Override
      @NonNull
      public List<UserPreferenceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfKeyword = CursorUtil.getColumnIndexOrThrow(_cursor, "keyword");
          final int _cursorIndexOfWeight = CursorUtil.getColumnIndexOrThrow(_cursor, "weight");
          final int _cursorIndexOfFeedback = CursorUtil.getColumnIndexOrThrow(_cursor, "feedback");
          final List<UserPreferenceEntity> _result = new ArrayList<UserPreferenceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UserPreferenceEntity _item;
            final String _tmpKeyword;
            _tmpKeyword = _cursor.getString(_cursorIndexOfKeyword);
            final double _tmpWeight;
            _tmpWeight = _cursor.getDouble(_cursorIndexOfWeight);
            final int _tmpFeedback;
            _tmpFeedback = _cursor.getInt(_cursorIndexOfFeedback);
            _item = new UserPreferenceEntity(_tmpKeyword,_tmpWeight,_tmpFeedback);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
