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
import com.example.notihub.parsers.KNUAnnouncementSource;
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
public final class KNUAnnouncementDao_Impl implements KNUAnnouncementDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<KNUAnnouncementEntity> __insertionAdapterOfKNUAnnouncementEntity;

  private final Converters __converters = new Converters();

  private final SharedSQLiteStatement __preparedStmtOfDeleteAnnouncement;

  public KNUAnnouncementDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfKNUAnnouncementEntity = new EntityInsertionAdapter<KNUAnnouncementEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `knu_announcement` (`source`,`id`,`title`,`time`,`bodyUrl`,`body`,`summary`,`keywords`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final KNUAnnouncementEntity entity) {
        final String _tmp = __converters.fromKNUAnnouncementSource(entity.getSource());
        statement.bindString(1, _tmp);
        statement.bindLong(2, entity.getId());
        statement.bindString(3, entity.getTitle());
        statement.bindString(4, entity.getTime());
        statement.bindString(5, entity.getBodyUrl());
        statement.bindString(6, entity.getBody());
        statement.bindString(7, entity.getSummary());
      }
    };
    this.__preparedStmtOfDeleteAnnouncement = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM knu_announcement WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertAnnouncement(final KNUAnnouncementEntity announcement,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfKNUAnnouncementEntity.insert(announcement);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAnnouncement(final int id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAnnouncement.acquire();
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
          __preparedStmtOfDeleteAnnouncement.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getAnnouncementById(final int id,
      final Continuation<? super KNUAnnouncementEntity> $completion) {
    final String _sql = "SELECT * FROM knu_announcement WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<KNUAnnouncementEntity>() {
      @Override
      @Nullable
      public KNUAnnouncementEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfBodyUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "bodyUrl");
          final int _cursorIndexOfBody = CursorUtil.getColumnIndexOrThrow(_cursor, "body");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfKeywords = CursorUtil.getColumnIndexOrThrow(_cursor, "keywords");
          final KNUAnnouncementEntity _result;
          if (_cursor.moveToFirst()) {
            final KNUAnnouncementSource _tmpSource;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfSource);
            _tmpSource = __converters.toKNUAnnouncementSource(_tmp);
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final String _tmpBodyUrl;
            _tmpBodyUrl = _cursor.getString(_cursorIndexOfBodyUrl);
            final String _tmpBody;
            _tmpBody = _cursor.getString(_cursorIndexOfBody);
            final String _tmpSummary;
            _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            final List<String> _tmpKeywords;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfKeywords);
            final List<String> _tmp_2 = __converters.toKeywords(_tmp_1);
            _tmpKeywords = _tmp_2;
            _result = new KNUAnnouncementEntity(_tmpSource,_tmpId,_tmpTitle,_tmpTime,_tmpBodyUrl,_tmpBody,_tmpSummary,_tmpKeywords);
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
  public Object getAllAnnouncements(
      final Continuation<? super List<KNUAnnouncementEntity>> $completion) {
    final String _sql = "SELECT * FROM knu_announcement ORDER BY time DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<KNUAnnouncementEntity>>() {
      @Override
      @NonNull
      public List<KNUAnnouncementEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfBodyUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "bodyUrl");
          final int _cursorIndexOfBody = CursorUtil.getColumnIndexOrThrow(_cursor, "body");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfKeywords = CursorUtil.getColumnIndexOrThrow(_cursor, "keywords");
          final List<KNUAnnouncementEntity> _result = new ArrayList<KNUAnnouncementEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final KNUAnnouncementEntity _item;
            final KNUAnnouncementSource _tmpSource;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfSource);
            _tmpSource = __converters.toKNUAnnouncementSource(_tmp);
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final String _tmpBodyUrl;
            _tmpBodyUrl = _cursor.getString(_cursorIndexOfBodyUrl);
            final String _tmpBody;
            _tmpBody = _cursor.getString(_cursorIndexOfBody);
            final String _tmpSummary;
            _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            final List<String> _tmpKeywords;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfKeywords);
            final List<String> _tmp_2 = __converters.toKeywords(_tmp_1);
            _tmpKeywords = _tmp_2;
            _item = new KNUAnnouncementEntity(_tmpSource,_tmpId,_tmpTitle,_tmpTime,_tmpBodyUrl,_tmpBody,_tmpSummary,_tmpKeywords);
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
