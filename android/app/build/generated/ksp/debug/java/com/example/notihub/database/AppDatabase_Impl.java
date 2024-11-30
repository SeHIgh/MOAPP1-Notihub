package com.example.notihub.database;

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
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile KNUAnnouncementDao _kNUAnnouncementDao;

  private volatile UserPreferenceDao _userPreferenceDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `knu_announcement` (`source` TEXT NOT NULL, `id` INTEGER NOT NULL, `title` TEXT NOT NULL, `time` TEXT NOT NULL, `bodyUrl` TEXT NOT NULL, `body` TEXT NOT NULL, `summary` TEXT NOT NULL, `keywords` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_preference` (`keyword` TEXT NOT NULL, `weight` REAL NOT NULL, `feedback` INTEGER NOT NULL, PRIMARY KEY(`keyword`))");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_user_preference_keyword` ON `user_preference` (`keyword`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '889fd405fe9b2912473bff8272a7c120')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `knu_announcement`");
        db.execSQL("DROP TABLE IF EXISTS `user_preference`");
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
        final HashMap<String, TableInfo.Column> _columnsKnuAnnouncement = new HashMap<String, TableInfo.Column>(8);
        _columnsKnuAnnouncement.put("source", new TableInfo.Column("source", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKnuAnnouncement.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKnuAnnouncement.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKnuAnnouncement.put("time", new TableInfo.Column("time", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKnuAnnouncement.put("bodyUrl", new TableInfo.Column("bodyUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKnuAnnouncement.put("body", new TableInfo.Column("body", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKnuAnnouncement.put("summary", new TableInfo.Column("summary", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKnuAnnouncement.put("keywords", new TableInfo.Column("keywords", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysKnuAnnouncement = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesKnuAnnouncement = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoKnuAnnouncement = new TableInfo("knu_announcement", _columnsKnuAnnouncement, _foreignKeysKnuAnnouncement, _indicesKnuAnnouncement);
        final TableInfo _existingKnuAnnouncement = TableInfo.read(db, "knu_announcement");
        if (!_infoKnuAnnouncement.equals(_existingKnuAnnouncement)) {
          return new RoomOpenHelper.ValidationResult(false, "knu_announcement(com.example.notihub.database.KNUAnnouncementEntity).\n"
                  + " Expected:\n" + _infoKnuAnnouncement + "\n"
                  + " Found:\n" + _existingKnuAnnouncement);
        }
        final HashMap<String, TableInfo.Column> _columnsUserPreference = new HashMap<String, TableInfo.Column>(3);
        _columnsUserPreference.put("keyword", new TableInfo.Column("keyword", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserPreference.put("weight", new TableInfo.Column("weight", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserPreference.put("feedback", new TableInfo.Column("feedback", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUserPreference = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUserPreference = new HashSet<TableInfo.Index>(1);
        _indicesUserPreference.add(new TableInfo.Index("index_user_preference_keyword", true, Arrays.asList("keyword"), Arrays.asList("ASC")));
        final TableInfo _infoUserPreference = new TableInfo("user_preference", _columnsUserPreference, _foreignKeysUserPreference, _indicesUserPreference);
        final TableInfo _existingUserPreference = TableInfo.read(db, "user_preference");
        if (!_infoUserPreference.equals(_existingUserPreference)) {
          return new RoomOpenHelper.ValidationResult(false, "user_preference(com.example.notihub.database.UserPreferenceEntity).\n"
                  + " Expected:\n" + _infoUserPreference + "\n"
                  + " Found:\n" + _existingUserPreference);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "889fd405fe9b2912473bff8272a7c120", "ca59327703bbf186055f51665f1b00e3");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "knu_announcement","user_preference");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `knu_announcement`");
      _db.execSQL("DELETE FROM `user_preference`");
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
    _typeConvertersMap.put(KNUAnnouncementDao.class, KNUAnnouncementDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(UserPreferenceDao.class, UserPreferenceDao_Impl.getRequiredConverters());
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
  public KNUAnnouncementDao knuAnnouncementDao() {
    if (_kNUAnnouncementDao != null) {
      return _kNUAnnouncementDao;
    } else {
      synchronized(this) {
        if(_kNUAnnouncementDao == null) {
          _kNUAnnouncementDao = new KNUAnnouncementDao_Impl(this);
        }
        return _kNUAnnouncementDao;
      }
    }
  }

  @Override
  public UserPreferenceDao userPreferenceDao() {
    if (_userPreferenceDao != null) {
      return _userPreferenceDao;
    } else {
      synchronized(this) {
        if(_userPreferenceDao == null) {
          _userPreferenceDao = new UserPreferenceDao_Impl(this);
        }
        return _userPreferenceDao;
      }
    }
  }
}
