package com.netmontools.lookatnet;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.netmontools.lookatnet.ui.point.model.DataModel;
import com.netmontools.lookatnet.ui.point.model.DataModelDao;
import com.netmontools.lookatnet.ui.remote.model.RemoteModel;
import com.netmontools.lookatnet.ui.remote.model.RemoteModelDao;


@Database(entities = {DataModel.class, RemoteModel.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DataModelDao dataModelDao();
    public abstract RemoteModelDao remoteModelDao();

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(final SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `RemoteModel` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `bssid` TEXT, `name` TEXT, `addr` TEXT, `login` TEXT, `pass` TEXT)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_RemoteModel_addr_bssid` ON `RemoteModel` (`addr`, `bssid`)");
        }
    };
}
